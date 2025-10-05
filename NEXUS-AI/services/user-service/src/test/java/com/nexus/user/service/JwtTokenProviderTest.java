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
    private final String testSecret = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz!@#$%^&*()_+0123456789"; // 80+ chars
    private final int testExpiration = 86400000; // 24 hours
    private final int testRefreshExpiration = 604800000; // 7 days

    @BeforeEach
    void setUp() throws Exception {
        jwtTokenProvider = new JwtTokenProvider();
        java.lang.reflect.Field secretField = JwtTokenProvider.class.getDeclaredField("jwtSecret");
        secretField.setAccessible(true);
        secretField.set(jwtTokenProvider, testSecret);

        java.lang.reflect.Field expField = JwtTokenProvider.class.getDeclaredField("jwtExpirationInMs");
        expField.setAccessible(true);
        expField.setInt(jwtTokenProvider, testExpiration);

        java.lang.reflect.Field refreshExpField = JwtTokenProvider.class.getDeclaredField("refreshExpirationInMs");
        refreshExpField.setAccessible(true);
        refreshExpField.setInt(jwtTokenProvider, testRefreshExpiration);
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
