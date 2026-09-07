package com.abhishek.portfolio.auth.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilTest {

    private static final String TEST_SECRET =
            "test-only-secret-key-with-at-least-32-bytes";
    private static final String DIFFERENT_TEST_SECRET =
            "different-test-only-secret-key-32-bytes";

    @Test
    void generateToken_shouldContainExpectedClaims() {
        // Arrange
        JwtUtil jwtUtil = new JwtUtil(TEST_SECRET, 60_000);

        // Act
        String token = jwtUtil.generateToken(
                "USR_000123",
                "user@example.com",
                "+919876543210",
                "USER"
        );

        // Assert
        assertThat(token).isNotBlank();
        assertThat(jwtUtil.extractUserId(token)).isEqualTo("USR_000123");
        assertThat(jwtUtil.extractEmail(token)).isEqualTo("user@example.com");
        assertThat(jwtUtil.extractRole(token)).isEqualTo("USER");
        assertThat(jwtUtil.isTokenValid(token)).isTrue();
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenTokenIsExpired() {
        // Arrange
        JwtUtil jwtUtil = new JwtUtil(TEST_SECRET, -1_000);
        String token = jwtUtil.generateToken(
                "USR_000123",
                "user@example.com",
                "+919876543210",
                "USER"
        );

        // Act
        boolean valid = jwtUtil.isTokenValid(token);

        // Assert
        assertThat(valid).isFalse();
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenTokenIsMalformed() {
        // Arrange
        JwtUtil jwtUtil = new JwtUtil(TEST_SECRET, 60_000);

        // Act
        boolean valid = jwtUtil.isTokenValid("not-a-jwt");

        // Assert
        assertThat(valid).isFalse();
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenTokenUsesIncorrectSecret() {
        // Arrange
        JwtUtil signingJwtUtil = new JwtUtil(DIFFERENT_TEST_SECRET, 60_000);
        JwtUtil verifyingJwtUtil = new JwtUtil(TEST_SECRET, 60_000);
        String token = signingJwtUtil.generateToken(
                "USR_000123",
                "user@example.com",
                "+919876543210",
                "USER"
        );

        // Act
        boolean valid = verifyingJwtUtil.isTokenValid(token);

        // Assert
        assertThat(valid).isFalse();
    }

    @Test
    void extractUserId_shouldRejectMalformedToken() {
        // Arrange
        JwtUtil jwtUtil = new JwtUtil(TEST_SECRET, 60_000);

        // Act / Assert
        assertThatThrownBy(() -> jwtUtil.extractUserId("not-a-jwt"))
                .isInstanceOf(RuntimeException.class);
    }
}
