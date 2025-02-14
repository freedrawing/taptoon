package com.sparta.taptoon.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Base64;

@Configuration
public class JwtConfig {
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Bean
    public String secretKey() {
        return Base64.getEncoder().encodeToString(jwtSecret.getBytes());
    }
}
