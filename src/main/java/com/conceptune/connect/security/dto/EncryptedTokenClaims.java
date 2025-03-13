package com.conceptune.connect.security.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
@AllArgsConstructor
public class EncryptedTokenClaims {
    private String subject;
    private String issuer;
    private Map<String, Object> claims;
    private Date expiration;

    public <T> T get(String key, Class<T> requiredType) {
        return requiredType.cast(claims.get(key));
    }
}
