package com.example.ridesharing.config;

import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Configuration
public class JwtConfig {

    // Load secret key from application.properties or use default if missing
    @Value("${jwt.secret:defaultSecretKeyForDevelopmentEnvironmentOnly}")
    private String secret;

    @Bean
    public SecretKey jwtSecretKey() {
        // Convert the secret string to a SecretKey using UTF-8 encoding
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
