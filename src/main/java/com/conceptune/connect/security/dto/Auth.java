package com.conceptune.connect.security.dto;

import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Log4j2
public class Auth implements Authentication {

    private final String token;
    private final String userId;
    private final String username;
    private final Map<String, Object> details;
    private boolean authenticated;


    public Auth(String token, String userId, String username, Map<String, Object> details) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.details = details;
        this.authenticated = false;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public Object getDetails() {
        return details;
    }

    @Override
    public Object getPrincipal() {
        return userId;
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        this.authenticated = isAuthenticated;
    }

    @Override
    public String getName() {
        return username;
    }
}
