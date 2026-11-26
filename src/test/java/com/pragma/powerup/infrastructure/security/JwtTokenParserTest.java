package com.pragma.powerup.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtTokenParserTest {
    private final JwtTokenParser jwtTokenParser = new JwtTokenParser(JwtTestTokens.SECRET);

    @Test
    void parseClaimsWithAValidTokenReturnsTheUserIdAndTheRole() {
        String token = JwtTestTokens.tokenFor(JwtTestTokens.SECRET, 5L, "OWNER");

        Claims claims = jwtTokenParser.parseClaims(token);

        assertEquals(5L, ((Number) claims.get(JwtTokenParser.USER_ID_CLAIM)).longValue());
        assertEquals("OWNER", claims.get(JwtTokenParser.ROLE_CLAIM, String.class));
    }

    @Test
    void parseClaimsWithATokenSignedWithAnotherSecretThrowsJwtException() {
        String foreignToken = JwtTestTokens.tokenFor(JwtTestTokens.OTHER_SECRET, 5L, "OWNER");

        assertThrows(JwtException.class, () -> jwtTokenParser.parseClaims(foreignToken));
    }

    @Test
    void parseClaimsWithAnExpiredTokenThrowsJwtException() {
        String expiredToken = JwtTestTokens.expiredTokenFor(JwtTestTokens.SECRET, 5L, "OWNER");

        assertThrows(JwtException.class, () -> jwtTokenParser.parseClaims(expiredToken));
    }

    @Test
    void parseClaimsWithAMalformedTokenThrowsJwtException() {
        assertThrows(JwtException.class, () -> jwtTokenParser.parseClaims("not-a-token"));
    }
}
