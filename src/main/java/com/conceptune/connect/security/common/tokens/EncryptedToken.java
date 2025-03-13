package com.conceptune.connect.security.common.tokens;

import com.conceptune.connect.security.exceptions.EncryptedTokenException;
import com.conceptune.connect.security.dto.EncryptedTokenClaims;
import com.conceptune.connect.security.common.crypto.AES256;
import com.conceptune.connect.utils.Generator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class EncryptedToken {

    private final ObjectMapper mapper = new ObjectMapper();
    private final String secretKey = Generator.generateString(32);

    @Autowired
    private AES256 aes;

    @Value("${connect.security.token.issuer}")
    private String ISSUER;

    private String getIssuer() {
        if (ISSUER == null) return "unknown";
        return ISSUER;
    }

    public String generate(String subject, Map<String, Object> claims, Date expiration) throws EncryptedTokenException {
        Map<String, Object> tokenMap = new HashMap<>();
        tokenMap.put("sub", subject);
        tokenMap.put("iss", getIssuer());
        tokenMap.put("exp", expiration.getTime());
        tokenMap.putAll(claims);

        try {
            return aes.encrypt(mapper.writeValueAsString(tokenMap), secretKey);
        } catch (Exception e) {
            throw new EncryptedTokenException("Error creating token.");
        }
    }

    public EncryptedTokenClaims validate(String token) throws EncryptedTokenException {
        try {
            String json = aes.decrypt(token, secretKey);
            Map<String, Object> tokenMap = mapper.readValue(json, new TypeReference<>() {});

            String subject = tokenMap.remove("sub").toString();
            String issuer = tokenMap.remove("iss").toString();
            Date expiration = new Date(Long.parseLong(tokenMap.remove("exp").toString()));
            Date current = new Date();

            if (!issuer.equals(getIssuer())) {
                throw new EncryptedTokenException("Invalid token issuer.");
            }

            if (current.after(expiration)) {
                throw new EncryptedTokenException("Token has expired.");
            }

            return new EncryptedTokenClaims(subject, issuer, tokenMap, expiration);
        } catch (Exception e) {
            throw new EncryptedTokenException("Token expired or invalid.");
        }
    }
}
