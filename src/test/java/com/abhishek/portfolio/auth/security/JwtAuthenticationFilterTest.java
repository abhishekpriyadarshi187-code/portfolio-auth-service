package com.abhishek.portfolio.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_shouldAuthenticateUser_whenBearerTokenIsValid() throws Exception {
        // Arrange
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtil);
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(jwtUtil.isTokenValid("valid-token")).thenReturn(true);
        when(jwtUtil.extractUserId("valid-token")).thenReturn("USR_000123");
        when(jwtUtil.extractRole("valid-token")).thenReturn("USER");

        // Act
        filter.doFilter(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isEqualTo("USR_000123");
        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_shouldNotAuthenticateUser_whenBearerTokenIsInvalid() throws Exception {
        // Arrange
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtil);
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token");
        when(jwtUtil.isTokenValid("invalid-token")).thenReturn(false);

        // Act
        filter.doFilter(request, response, filterChain);

        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtUtil, never()).extractUserId("invalid-token");
        verify(jwtUtil, never()).extractRole("invalid-token");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilter_shouldContinueWithoutAuthentication_whenAuthorizationHeaderIsMissing()
            throws Exception {
        // Arrange
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtil);

        // Act
        filter.doFilter(request, response, filterChain);

        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtUtil, never()).isTokenValid(org.mockito.ArgumentMatchers.any());
        verify(filterChain).doFilter(request, response);
    }
}
