package com.nexus.user.service;

import com.nexus.user.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private final String testSecret = "testSecretKeyForJunitTests256BitsLong";
    private final long testExpiration = 86400000L; // 24 hours

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        // Set test values using reflection or constructor if available
        // This assumes you have setters or can inject these values
    }

    @Test
    void shouldGenerateValidToken() {
        // Given
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "testuser",
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        // When
        String token = jwtTokenProvider.generateToken(auth);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    void shouldExtractUsernameFromToken() {
        // Given
        Authentication auth = new UsernamePasswordAuthenticationToken("testuser", null);
        String token = jwtTokenProvider.generateToken(auth);

        // When
        String username = jwtTokenProvider.getUsernameFromToken(token);

        // Then
        assertThat(username).isEqualTo("testuser");
    }

    @Test
    void shouldValidateValidToken() {
        // Given
        Authentication auth = new UsernamePasswordAuthenticationToken("testuser", null);
        String token = jwtTokenProvider.generateToken(auth);

        // When
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Then
        assertThat(isValid).isTrue();
    }
}
