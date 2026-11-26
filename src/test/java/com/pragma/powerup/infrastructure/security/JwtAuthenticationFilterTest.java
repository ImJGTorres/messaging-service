package com.pragma.powerup.infrastructure.security;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtAuthenticationFilterTest {
    private static final String AUTHORIZATION = "Authorization";

    private JwtAuthenticationFilter jwtAuthenticationFilter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        jwtAuthenticationFilter = new JwtAuthenticationFilter(new JwtTokenParser(JwtTestTokens.SECRET));
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterWithAValidTokenAuthenticatesWithTheUserIdAndTheRoleAuthority()
            throws ServletException, IOException {
        request.addHeader(AUTHORIZATION, "Bearer " + JwtTestTokens.tokenFor(JwtTestTokens.SECRET, 5L, "OWNER"));

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(5L, authentication.getPrincipal());
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_OWNER".equals(authority.getAuthority())));
        assertNotNull(filterChain.getRequest());
    }

    @Test
    void doFilterWithoutAuthorizationHeaderLeavesTheRequestUnauthenticatedAndContinues()
            throws ServletException, IOException {
        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNotNull(filterChain.getRequest());
    }

    @Test
    void doFilterWithAnInvalidTokenLeavesTheRequestUnauthenticatedAndContinues()
            throws ServletException, IOException {
        request.addHeader(AUTHORIZATION, "Bearer not-a-token");

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNotNull(filterChain.getRequest());
    }

    @Test
    void doFilterWithATokenSignedWithAnotherSecretLeavesTheRequestUnauthenticatedAndContinues()
            throws ServletException, IOException {
        request.addHeader(AUTHORIZATION,
                "Bearer " + JwtTestTokens.tokenFor(JwtTestTokens.OTHER_SECRET, 5L, "OWNER"));

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNotNull(filterChain.getRequest());
    }

    @Test
    void doFilterWithAnExpiredTokenLeavesTheRequestUnauthenticatedAndContinues()
            throws ServletException, IOException {
        request.addHeader(AUTHORIZATION,
                "Bearer " + JwtTestTokens.expiredTokenFor(JwtTestTokens.SECRET, 5L, "OWNER"));

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNotNull(filterChain.getRequest());
    }

    @Test
    void doFilterWithAnEmptyBearerTokenLeavesTheRequestUnauthenticatedAndContinues()
            throws ServletException, IOException {
        request.addHeader(AUTHORIZATION, "Bearer ");

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNotNull(filterChain.getRequest());
    }

    @Test
    void doFilterWithANonBearerSchemeIgnoresTheHeaderAndContinues() throws ServletException, IOException {
        request.addHeader(AUTHORIZATION, "Basic dXNlcjpwYXNz");

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNotNull(filterChain.getRequest());
    }
}
