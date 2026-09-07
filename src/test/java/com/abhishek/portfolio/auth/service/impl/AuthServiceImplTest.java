package com.abhishek.portfolio.auth.service.impl;

import com.abhishek.portfolio.auth.dto.AuthResponse;
import com.abhishek.portfolio.auth.dto.LoginRequest;
import com.abhishek.portfolio.auth.dto.RegisterRequest;
import com.abhishek.portfolio.auth.dto.RegisterResponse;
import com.abhishek.portfolio.auth.exception.BaseException;
import com.abhishek.portfolio.auth.exception.InvalidCredentialsException;
import com.abhishek.portfolio.auth.exception.UserAlreadyExistsException;
import com.abhishek.portfolio.auth.exception.UserNotFoundException;
import com.abhishek.portfolio.auth.model.User;
import com.abhishek.portfolio.auth.repository.UserRepository;
import com.abhishek.portfolio.auth.security.JwtUtil;
import com.abhishek.portfolio.auth.service.SequenceGeneratorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    private static final String RAW_PASSWORD = "Test-only1!";
    private static final String ENCODED_PASSWORD = "encoded-test-password";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SequenceGeneratorService sequenceGeneratorService;

    @Mock
    private JwtUtil jwtUtil;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(
                userRepository,
                passwordEncoder,
                sequenceGeneratorService,
                jwtUtil
        );
    }

    @Test
    void register_shouldCreateAdminWithNormalizedData_whenRegisteringFirstUser() {
        // Arrange
        RegisterRequest request = registerRequest(
                "  ADMIN@Example.COM  ",
                " +91 98765-43210 ",
                "  Test Administrator  "
        );
        when(userRepository.count()).thenReturn(0L);
        when(sequenceGeneratorService.generateSequence("user_sequence")).thenReturn(42L);
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        RegisterResponse response = authService.register(request);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertThat(savedUser.getUserId()).isEqualTo("USR_000042");
        assertThat(savedUser.getEmail()).isEqualTo("admin@example.com");
        assertThat(savedUser.getMobileNumber()).isEqualTo("+919876543210");
        assertThat(savedUser.getFullName()).isEqualTo("Test Administrator");
        assertThat(savedUser.getPassword()).isEqualTo(ENCODED_PASSWORD);
        assertThat(savedUser.getRole()).isEqualTo("ADMIN");
        assertThat(savedUser.isEnabled()).isTrue();
        assertThat(savedUser.isAccountNonLocked()).isTrue();
        assertThat(savedUser.isEmailVerified()).isFalse();
        assertThat(savedUser.isMobileNumberVerified()).isFalse();
        assertThat(savedUser.isTwoFactorEnabled()).isFalse();
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isEqualTo(savedUser.getCreatedAt());

        assertThat(response.getMessage()).isEqualTo("User registered successfully");
        assertThat(response.getUserId()).isEqualTo("USR_000042");
        assertThat(response.getEmail()).isEqualTo("admin@example.com");
        assertThat(response.getMobileNumber()).isEqualTo("+919876543210");
        assertThat(response.getFullName()).isEqualTo("Test Administrator");
        assertThat(response.getRole()).isEqualTo("ADMIN");
        verify(passwordEncoder).encode(RAW_PASSWORD);
    }

    @Test
    void register_shouldCreateRegularUser_whenAnotherUserAlreadyExists() {
        // Arrange
        RegisterRequest request = registerRequest(
                "user@example.com",
                "+919876543211",
                "Test User"
        );
        when(userRepository.count()).thenReturn(1L);
        when(sequenceGeneratorService.generateSequence("user_sequence")).thenReturn(7L);
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        RegisterResponse response = authService.register(request);

        // Assert
        assertThat(response.getUserId()).isEqualTo("USR_000007");
        assertThat(response.getRole()).isEqualTo("USER");
    }

    @Test
    void register_shouldRejectDuplicateEmail() {
        // Arrange
        RegisterRequest request = registerRequest(
                " Existing@Example.com ",
                "+919876543210",
                "Existing User"
        );
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // Act / Assert
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("User already exists with email: existing@example.com");

        verify(userRepository, never()).existsByMobileNumber(any());
        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder, sequenceGeneratorService, jwtUtil);
    }

    @Test
    void register_shouldRejectDuplicateMobileNumber() {
        // Arrange
        RegisterRequest request = registerRequest(
                "new@example.com",
                "+91 98765-43210",
                "New User"
        );
        when(userRepository.existsByMobileNumber("+919876543210")).thenReturn(true);

        // Act / Assert
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessage("User already exists with email: +919876543210");

        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder, sequenceGeneratorService, jwtUtil);
    }

    @Test
    void login_shouldReturnToken_whenEmailCredentialsAreValid() {
        // Arrange
        LoginRequest request = loginRequest(" User@Example.COM ", RAW_PASSWORD);
        User user = user(true, false);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
        when(jwtUtil.generateToken(
                "USR_000123",
                "user@example.com",
                "+919876543210",
                "USER"
        )).thenReturn("test.jwt.token");

        // Act
        AuthResponse response = authService.login(request);

        // Assert
        assertThat(response.getToken()).isEqualTo("test.jwt.token");
        assertThat(response.getUserId()).isEqualTo("USR_000123");
        assertThat(response.getRole()).isEqualTo("USER");
        assertThat(response.isRequiresTwoFactor()).isFalse();
        verify(userRepository).findByEmail("user@example.com");
        verify(userRepository, never()).findByMobileNumber(any());
    }

    @Test
    void login_shouldReturnToken_whenMobileCredentialsAreValid() {
        // Arrange
        LoginRequest request = loginRequest(" +91 98765-43210 ", RAW_PASSWORD);
        User user = user(true, true);
        when(userRepository.findByMobileNumber("+919876543210")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
        when(jwtUtil.generateToken(any(), any(), any(), any())).thenReturn("test.jwt.token");

        // Act
        AuthResponse response = authService.login(request);

        // Assert
        assertThat(response.getToken()).isEqualTo("test.jwt.token");
        assertThat(response.isRequiresTwoFactor()).isTrue();
        verify(userRepository).findByMobileNumber("+919876543210");
        verify(userRepository, never()).findByEmail(any());
        verify(jwtUtil).generateToken(
                "USR_000123",
                "user@example.com",
                "+919876543210",
                "USER"
        );
    }

    @Test
    void login_shouldRejectRequest_whenEmailDoesNotExist() {
        // Arrange
        LoginRequest request = loginRequest("missing@example.com", RAW_PASSWORD);
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        // Act / Assert
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with email: missing@example.com");

        verifyNoInteractions(passwordEncoder, jwtUtil);
    }

    @Test
    void login_shouldRejectRequest_whenMobileNumberDoesNotExist() {
        // Arrange
        LoginRequest request = loginRequest("+919999999999", RAW_PASSWORD);
        when(userRepository.findByMobileNumber("+919999999999")).thenReturn(Optional.empty());

        // Act / Assert
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with email: +919999999999");

        verifyNoInteractions(passwordEncoder, jwtUtil);
    }

    @Test
    void login_shouldRejectRequest_whenPasswordIsIncorrect() {
        // Arrange
        LoginRequest request = loginRequest("user@example.com", "Wrong-test1!");
        User user = user(true, false);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Wrong-test1!", ENCODED_PASSWORD)).thenReturn(false);

        // Act / Assert
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid email or password");

        verifyNoInteractions(jwtUtil);
    }

    @Test
    void login_shouldRejectRequest_whenAccountIsDisabled() {
        // Arrange
        LoginRequest request = loginRequest("user@example.com", RAW_PASSWORD);
        User user = user(false, false);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);

        // Act / Assert
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BaseException.class)
                .hasMessage("Account is disabled")
                .satisfies(exception -> assertThat(((BaseException) exception).getStatus().value())
                        .isEqualTo(403));

        verifyNoInteractions(jwtUtil);
    }

    private RegisterRequest registerRequest(String email, String mobileNumber, String fullName) {
        RegisterRequest request = new RegisterRequest();
        request.setEmail(email);
        request.setMobileNumber(mobileNumber);
        request.setFullName(fullName);
        request.setPassword(RAW_PASSWORD);
        return request;
    }

    private LoginRequest loginRequest(String identifier, String password) {
        LoginRequest request = new LoginRequest();
        request.setIdentifier(identifier);
        request.setPassword(password);
        return request;
    }

    private User user(boolean enabled, boolean twoFactorEnabled) {
        return User.builder()
                .id("mongo-id")
                .userId("USR_000123")
                .email("user@example.com")
                .mobileNumber("+919876543210")
                .fullName("Test User")
                .password(ENCODED_PASSWORD)
                .role("USER")
                .enabled(enabled)
                .accountNonLocked(true)
                .twoFactorEnabled(twoFactorEnabled)
                .createdAt(LocalDateTime.of(2026, 1, 1, 12, 0))
                .updatedAt(LocalDateTime.of(2026, 1, 1, 12, 0))
                .build();
    }
}
