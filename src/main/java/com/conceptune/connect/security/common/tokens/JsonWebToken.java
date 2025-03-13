package com.conceptune.connect.security.common.tokens;

import com.conceptune.connect.utils.Generator;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;

@Log4j2
@Component
public class JsonWebToken {
    public final static int DEFAULT = 0;
    public final static int ACCESS = 1;
    public final static int REFRESH = 2;

    private final String defaultKey = Generator.generateString(32);
    private final String accessKey = Generator.generateString(32);
    private final String refreshKey = Generator.generateString(32);

    @Value("{connect.security.token.default-key}")
    private String DEFAULT_KEY;

    @Value("{connect.security.token.access-key}")
    private String ACCESS_KEY;

    @Value("{connect.security.token.refresh-key}")
    private String REFRESH_KEY;

    @Value("${connect.security.token.issuer}")
    private String ISSUER;

    private String getIssuer() {
        if (ISSUER == null) return "unknown";
        return ISSUER;
    }

    private String getDefaultKey() {
        if (DEFAULT_KEY == null) return defaultKey;
        return DEFAULT_KEY;
    }

    private String getAccessKey() {
        if (ACCESS_KEY == null) return accessKey;
        return ACCESS_KEY;
    }

    private String getRefreshKey() {
        if (REFRESH_KEY == null) return refreshKey;
        return REFRESH_KEY;
    }

    public String generate(int tokenType, String subject, Map<String, Object> claims, Date expiration) {
        Key key = switch (tokenType) {
            case JsonWebToken.ACCESS -> Keys.hmacShaKeyFor(getAccessKey().getBytes(StandardCharsets.UTF_8));
            case JsonWebToken.REFRESH -> Keys.hmacShaKeyFor(getRefreshKey().getBytes(StandardCharsets.UTF_8));
            default -> Keys.hmacShaKeyFor(getDefaultKey().getBytes(StandardCharsets.UTF_8));
        };
        return Jwts.builder().subject(subject).issuer(getIssuer()).expiration(expiration).claims(claims).issuedAt(new Date()).signWith(key).compact();
    }

    public Claims validate(int tokenType, String token) throws JwtException {
        try {
            SecretKey key = switch (tokenType) {
                case JsonWebToken.ACCESS -> Keys.hmacShaKeyFor(getAccessKey().getBytes(StandardCharsets.UTF_8));
                case JsonWebToken.REFRESH -> Keys.hmacShaKeyFor(getRefreshKey().getBytes(StandardCharsets.UTF_8));
                default -> Keys.hmacShaKeyFor(getDefaultKey().getBytes(StandardCharsets.UTF_8));
            };
            return Jwts.parser().verifyWith(key).requireIssuer(getIssuer()).build().parseSignedClaims(token).getPayload();
        } catch (Exception e) {
            throw new JwtException("Token expired or invalid.");
        }
    }
}
