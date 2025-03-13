package com.conceptune.connect.services;

import com.conceptune.connect.database.models.MessageToken;
import com.conceptune.connect.database.repository.MessageTokenRepository;
import com.conceptune.connect.security.dto.EncryptedTokenClaims;
import com.conceptune.connect.security.common.crypto.Hash;
import com.conceptune.connect.security.common.crypto.RSA;
import com.conceptune.connect.security.common.tokens.EncryptedToken;
import com.conceptune.connect.security.common.tokens.JsonWebToken;
import com.conceptune.connect.settings.InstanceVariable;
import com.conceptune.connect.utils.Generator;
import com.conceptune.connect.utils.Response;
import com.conceptune.connect.constants.MultiFactorAuthStatus;
import com.conceptune.connect.constants.TokenType;
import com.conceptune.connect.database.models.EncryptionKey;
import com.conceptune.connect.database.models.LoginState;
import com.conceptune.connect.database.models.User;
import com.conceptune.connect.database.repository.EncryptionKeyRepository;
import com.conceptune.connect.database.repository.LoginStateRepository;
import com.conceptune.connect.database.repository.UserRepository;
import com.conceptune.connect.exceptions.AuthException;
import com.conceptune.connect.dto.request.NewAccount;
import com.conceptune.connect.dto.response.AuthToken;
import com.conceptune.connect.dto.request.SignIn;
import com.conceptune.connect.dto.request.OtpPayload;
import com.conceptune.connect.dto.response.NewUserToken;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.KeyPair;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Log4j2
@RequiredArgsConstructor
public class AuthService {

    private final JsonWebToken jwt;
    private final EncryptedToken et;
    private final RSA rsa;
    private final Hash hash;
    private final UserRepository userRepository;
    private final LoginStateRepository loginStateRepository;
    private final EncryptionKeyRepository encryptionKeyRepository;
    private final MessageTokenRepository messageTokenRepository;
    private final FirebaseAuth firebaseAuth;

    @Value("${connect.test.security.auth.phone}")
    private String TEST_PHONE;

    @Value("${connect.test.security.auth.otp}")
    private String TEST_OTP;

    @Value("${connect.security.auth.otp.expiration}")
    private Long OTP_EXPIRE_MILLIS;

    @Value("${connect.security.auth.access.expiration}")
    private Long ACCESS_EXPIRY_MILLIS;

    @Value("${connect.security.auth.refresh.expiration}")
    private Long REFRESH_EXPIRY_MILLIS;

    @Value("${connect.security.auth.identity.expiration}")
    private Long IDENTITY_EXPIRY_MILLIS;

    /**
     * Initiate user sign-in
     * @param signIn sign-in payload
     * @return sign-in token
     * @throws Exception if an error occurs
     */
    public String signIn(SignIn signIn) throws Exception {
        Map<String , Object> claims = new HashMap<>();
        claims.put("country", signIn.getCountry());
        claims.put("countryCode", signIn.getCountryCode());
        claims.put("phone", signIn.getPhone());
        claims.put("messageToken", signIn.getMessageToken());

        try {
            if (!TEST_PHONE.equals(signIn.getPhone())) {
                String phone = signIn.getCountryCode() + signIn.getPhone();
                Verification.creator(InstanceVariable.TWILIO_SERVICE_ID, phone, "sms").create();
            }
        } catch (Exception e) {
            throw new AuthException(e.getMessage());
        }

        return et.generate(TokenType.SIGN_IN.getValue(), claims, new Date(System.currentTimeMillis() + OTP_EXPIRE_MILLIS));
    }

    /**
     * Verification for otp
     * @param token otp token
     * @param payload otp payload
     * @return Response model
     * @throws Exception if an error occurs
     */
    public Response<?> verify(String token, OtpPayload payload) throws Exception {
        EncryptedTokenClaims claims = et.validate(token);

        String countryCode = claims.get("countryCode", String.class);
        String phone = claims.get("phone", String.class);

        try {
            if (TEST_PHONE.equals(phone)) {
                if (!TEST_OTP.equals(payload.getOtp())) {
                    return Response.error("Invalid OTP.");
                }
            } else {
                VerificationCheck verificationCheck = VerificationCheck.creator(InstanceVariable.TWILIO_SERVICE_ID).setTo(countryCode + phone).setCode(payload.getOtp()).create();

                if (!verificationCheck.getStatus().equals("approved")) {
                    return Response.error("Invalid OTP.");
                }
            }
        } catch (Exception e) {
            throw new AuthException(e.getMessage());
        }

        return switch (TokenType.valueOf(claims.getSubject())) {
            case SIGN_IN -> completeSignIn(claims);
            default -> throw new AuthException("Invalid verification.");
        };
    }

    /**
     * Refresh current auth state of user with new authentication tokens
     * @param refreshToken Refresh token
     * @param loginStateToken Login state token
     * @return Response model
     */
    public Response<?> refresh(String refreshToken, String loginStateToken) throws Exception {
        Claims claims = jwt.validate(JsonWebToken.REFRESH, refreshToken);
        User user = userRepository.findById(claims.getSubject());
        LoginState state = loginStateRepository.findById(claims.get("state").toString());

        if (user == null || state == null || !loginStateToken.equals(state.getToken())) {
            return Response.error("Unable to refresh credentials.");
        }

        state.setToken(Generator.generateToken(40));
        state.setCreatedAt(Timestamp.from(Instant.now()));

        boolean isLoginStateUpdated = loginStateRepository.updateToken(state.getId(), state.getUserId(), state.getToken(), state.getCreatedAt());

        if (!isLoginStateUpdated) {
            return Response.error("Unable to refresh state.");
        }

        return getAuthTokenResponse(user, state, false);
    }

    /**
     * Completes sign-in process
     * @param claims Encrypted token claims
     * @return Response model
     * @throws Exception if an error occurs
     */
    @Transactional
    private Response<?> completeSignIn(EncryptedTokenClaims claims) throws Exception {
        String country = claims.get("country", String.class);
        String countryCode = claims.get("countryCode", String.class);
        String phone = claims.get("phone", String.class);
        String messageTokenString = claims.get("messageToken", String.class);

        User user = userRepository.findByPhone(phone);

        if (user == null) {
            Map<String , Object> newClaims = new HashMap<>();
            newClaims.put("country", country);
            newClaims.put("countryCode", countryCode);
            newClaims.put("phone", phone);
            newClaims.put("messageToken", messageTokenString);

            String token = et.generate(TokenType.NEW_USER.getValue(), newClaims, new Date(System.currentTimeMillis() + IDENTITY_EXPIRY_MILLIS));
            NewUserToken userToken = new NewUserToken(token, true);
            return Response.success("New User", userToken);
        }

        LoginState state = new LoginState();
        state.setId(Generator.generateString(10));
        state.setUserId(user.getId());
        state.setToken(Generator.generateToken(40));
        state.setCreatedAt(Timestamp.from(Instant.now()));

        boolean isLoginStateCreated = loginStateRepository.save(state);
        boolean isMessageTokenAdded;

        if (messageTokenRepository.isExistByUser(user.getId())) {
            isMessageTokenAdded = messageTokenRepository.updateToken(user.getId(), messageTokenString, Timestamp.from(Instant.now()));
        } else {
            MessageToken messageToken = new MessageToken();
            messageToken.setUserId(user.getId());
            messageToken.setToken(messageTokenString);
            messageToken.setCreatedAt(Timestamp.from(Instant.now()));
            isMessageTokenAdded = messageTokenRepository.save(messageToken);
        }

        if (isLoginStateCreated && isMessageTokenAdded) {
            return getAuthTokenResponse(user, state, false);
        }

        return Response.error("Unable to sign-in.");
    }

    /**
     * Create new user and sign-in
     * @param token create new user token
     * @param account New Account details Model
     * @return Response model
     * @throws Exception if an error occurs
     */
    @Transactional
    public Response<?> createNewUserAndSignIn(String token, NewAccount account) throws Exception {
        EncryptedTokenClaims claims = et.validate(token);
        String country = claims.get("country", String.class);
        String countryCode = claims.get("countryCode", String.class);
        String phone = claims.get("phone", String.class);
        String messageTokenString = claims.get("messageToken", String.class);
        String phoneWithCountryCode = countryCode.trim().concat(phone.trim());

        KeyPair pair = rsa.generateKeyPair();
        String publicKey = rsa.encodePublicKey(pair.getPublic());
        String privateKey = rsa.encodePrivateKey(pair.getPrivate());
        String secretKey = Generator.generateString(32);
        String userId = UUID.randomUUID().toString();
        Timestamp currentTimeStamp = Timestamp.from(Instant.now());

        User user = new User();
        user.setId(userId);
        user.setName(account.getName());
        user.setUsername(Generator.generateUsername(account.getName()));
        user.setPhone(phone);
        user.setHash(hash.createSha256(phoneWithCountryCode));
        user.setPhoto(null);
        user.setEmail(null);
        user.setPin(null);
        user.setCountry(country);
        user.setCountryCode(countryCode);
        user.setTitle("Hey there! I’m on Connect");
        user.setActive(true);
        user.setMfaStatus(MultiFactorAuthStatus.DISABLED.getValue());
        user.setCreatedAt(currentTimeStamp);
        user.setUpdatedAt(currentTimeStamp);

        EncryptionKey encryptionKey = new EncryptionKey();
        encryptionKey.setUserId(userId);
        encryptionKey.setPublicKey(publicKey);
        encryptionKey.setPrivateKey(privateKey);
        encryptionKey.setSecretKey(secretKey);
        encryptionKey.setCreatedAt(currentTimeStamp);

        LoginState state = new LoginState();
        state.setId(Generator.generateString(10));
        state.setUserId(userId);
        state.setToken(Generator.generateToken(40));
        state.setCreatedAt(currentTimeStamp);

        MessageToken messageToken = new MessageToken();
        messageToken.setUserId(userId);
        messageToken.setToken(messageTokenString);
        messageToken.setCreatedAt(currentTimeStamp);

        boolean isUserCreated = userRepository.save(user);
        boolean isEncryptionKeyCreated = encryptionKeyRepository.save(encryptionKey);
        boolean isLoginStateCreated = loginStateRepository.save(state);
        boolean isMessageTokenCreated = messageTokenRepository.save(messageToken);

        if (isUserCreated && isEncryptionKeyCreated && isLoginStateCreated && isMessageTokenCreated) {
            UserRecord.CreateRequest userRecord = new UserRecord.CreateRequest();
            userRecord.setUid(userId);
            userRecord.setPhoneNumber(phoneWithCountryCode);
            userRecord.setDisplayName(account.getName());
            userRecord.setDisabled(false);
            firebaseAuth.createUser(userRecord);

            return getAuthTokenResponse(user, state, false);
        }

        throw new AuthException("Unable to create account.");
    }

    /**
     * Generate auth token Http Response
     * @param user User
     * @param state Login State
     * @return Response model consisting of auth tokens
     */
    private Response<?> getAuthTokenResponse(User user, LoginState state, boolean newUser) throws Exception {
        AuthToken token = generateAuthTokens(user, state, newUser);
        return Response.success("Authenticated", token);
    }

    /**
     * Generates authentication tokens
     * @param user User for which to generate authentication tokens
     * @return AuthToken Model
     */
    private AuthToken generateAuthTokens(User user, LoginState state, boolean newUser) throws Exception {
        try {
            Map<String, Object> accessClaims = Map.of("username", user.getUsername(), "country", user.getCountry());
            Map<String, Object> refreshClaims = Map.of("state", state.getId(), "country", user.getCountry());

            String accessToken = jwt.generate(JsonWebToken.ACCESS, user.getId(), accessClaims, new Date(System.currentTimeMillis() + ACCESS_EXPIRY_MILLIS));
            String refreshToken = jwt.generate(JsonWebToken.REFRESH, user.getId(), refreshClaims, new Date(System.currentTimeMillis() + REFRESH_EXPIRY_MILLIS));
            String firebaseAuthToken = firebaseAuth.createCustomToken(user.getId());

            return new AuthToken(user.getId(), accessToken, refreshToken, state.getToken(), firebaseAuthToken, newUser);
        } catch (Exception e) {
            throw new AuthException(e.getMessage());
        }
    }
}
