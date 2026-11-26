package com.pragma.powerup.infrastructure.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;

final class JwtTestTokens {
    static final String SECRET = Base64.getEncoder()
            .encodeToString("unit-test-secret-key-with-more-than-32-bytes".getBytes(StandardCharsets.UTF_8));
    static final String OTHER_SECRET = Base64.getEncoder()
            .encodeToString("another-unit-test-secret-key-with-32-bytes+".getBytes(StandardCharsets.UTF_8));
    private static final long ONE_HOUR_MILLIS = 3_600_000L;

    private JwtTestTokens() {
    }

    static String tokenFor(String secret, Long userId, String role) {
        return buildToken(secret, userId, role, ONE_HOUR_MILLIS);
    }

    static String expiredTokenFor(String secret, Long userId, String role) {
        return buildToken(secret, userId, role, -ONE_HOUR_MILLIS);
    }

    private static String buildToken(String secret, Long userId, String role, long lifetimeMillis) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject("user" + userId + "@correo.com")
                .claim("userId", userId)
                .claim("role", role)
                .issuedAt(new Date(now))
                .expiration(new Date(now + lifetimeMillis))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret)))
                .compact();
    }
}
