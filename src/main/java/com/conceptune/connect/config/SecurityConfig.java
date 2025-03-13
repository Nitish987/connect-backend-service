package com.conceptune.connect.config;

import com.conceptune.connect.security.repository.AuthContextRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Log4j2
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${connect.security.cors.allow-origins}")
    private String CORS_ALLOW_ORIGINS;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http, AuthContextRepository authContextRepository) {
        http.httpBasic(ServerHttpSecurity.HttpBasicSpec::disable);
        http.formLogin(ServerHttpSecurity.FormLoginSpec::disable);
        http.logout(ServerHttpSecurity.LogoutSpec::disable);
        http.csrf(ServerHttpSecurity.CsrfSpec::disable);
        http.cors(corsSpec -> corsSpec.configurationSource(corsConfigurationSource()));
        http.authorizeExchange(authorizeExchangeSpec -> {
            authorizeExchangeSpec.pathMatchers("/api/auth/**").permitAll();
            authorizeExchangeSpec.pathMatchers("/ws/**").permitAll();
            authorizeExchangeSpec.anyExchange().authenticated();
        });
        http.securityContextRepository(authContextRepository);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(CORS_ALLOW_ORIGINS.split(",")));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        configuration.setAllowedHeaders(List.of(HttpHeaders.AUTHORIZATION, HttpHeaders.CONTENT_TYPE, "Cherry", "Cot", "Cnut", "Rt", "Lst"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
