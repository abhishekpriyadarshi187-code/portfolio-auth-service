package com.abhishek.portfolio.auth.controller;

import com.abhishek.portfolio.auth.dto.AuthResponse;
import com.abhishek.portfolio.auth.dto.LoginRequest;
import com.abhishek.portfolio.auth.dto.RegisterRequest;
import com.abhishek.portfolio.auth.dto.RegisterResponse;
import com.abhishek.portfolio.auth.exception.GlobalExceptionHandler;
import com.abhishek.portfolio.auth.exception.InvalidCredentialsException;
import com.abhishek.portfolio.auth.exception.UserAlreadyExistsException;
import com.abhishek.portfolio.auth.security.JwtAuthenticationFilter;
import com.abhishek.portfolio.auth.security.JwtUtil;
import com.abhishek.portfolio.auth.security.SecurityConfig;
import com.abhishek.portfolio.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = {AuthController.class, HealthController.class},
        properties = "cors.allowed-origins=https://test.example"
)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class, GlobalExceptionHandler.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    void register_shouldReturnCreatedResponse_whenRequestIsValid() throws Exception {
        // Arrange
        RegisterResponse response = RegisterResponse.builder()
                .message("User registered successfully")
                .userId("USR_000001")
                .email("user@example.com")
                .mobileNumber("+919876543210")
                .fullName("Test User")
                .role("ADMIN")
                .build();
        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        // Act / Assert
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRegisterJson()))
                .andExpect(status().isCreated())
                .andExpect(unauthenticated())
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.userId").value("USR_000001"))
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.mobileNumber").value("+919876543210"))
                .andExpect(jsonPath("$.fullName").value("Test User"))
                .andExpect(jsonPath("$.role").value("ADMIN"));

        verify(authService).register(any(RegisterRequest.class));
    }

    @Test
    void login_shouldReturnAuthenticationResponse_whenRequestIsValid() throws Exception {
        // Arrange
        AuthResponse response = AuthResponse.builder()
                .token("test.jwt.token")
                .requiresTwoFactor(false)
                .role("USER")
                .userId("USR_000123")
                .build();
        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        // Act / Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "identifier": "user@example.com",
                                  "password": "Test-only1!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(unauthenticated())
                .andExpect(jsonPath("$.token").value("test.jwt.token"))
                .andExpect(jsonPath("$.requiresTwoFactor").value(false))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.userId").value("USR_000123"));

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    void register_shouldReturnBadRequest_whenEmailFormatIsInvalid() throws Exception {
        // Act / Assert
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "not-an-email",
                                  "mobileNumber": "+919876543210",
                                  "fullName": "Test User",
                                  "password": "Test-only1!"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid email format"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.timestamp").exists());

        verifyNoInteractions(authService);
    }

    @Test
    void register_shouldReturnBadRequest_whenPasswordIsWeak() throws Exception {
        // Act / Assert
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "mobileNumber": "+919876543210",
                                  "fullName": "Test User",
                                  "password": "weak"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Password must be 8-128 characters long and contain at least one uppercase letter, "
                                + "one lowercase letter, one number, and one special character"
                ))
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(authService);
    }

    @Test
    void login_shouldReturnBadRequest_whenIdentifierIsBlank() throws Exception {
        // Act / Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "identifier": " ",
                                  "password": "Test-only1!"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email or mobile number cannot be blank"))
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(authService);
    }

    @Test
    void register_shouldMapDuplicateUserExceptionToBadRequest() throws Exception {
        // Arrange
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new UserAlreadyExistsException("user@example.com"));

        // Act / Assert
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRegisterJson()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("User already exists with email: user@example.com"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void login_shouldMapInvalidCredentialsExceptionToUnauthorized() throws Exception {
        // Arrange
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new InvalidCredentialsException());

        // Act / Assert
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "identifier": "user@example.com",
                                  "password": "Wrong-test1!"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void register_shouldUseGenericErrorMapping_whenJsonIsMalformed() throws Exception {
        // Act / Assert
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Something went wrong"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.timestamp").exists());

        verifyNoInteractions(authService);
    }

    @Test
    void profile_shouldRequireAuthentication_underApplicationSecurityConfiguration()
            throws Exception {
        // Act / Assert
        mockMvc.perform(get("/profile"))
                .andExpect(status().isForbidden())
                .andExpect(unauthenticated());
    }

    private String validRegisterJson() {
        return """
                {
                  "email": "user@example.com",
                  "mobileNumber": "+919876543210",
                  "fullName": "Test User",
                  "password": "Test-only1!"
                }
                """;
    }
}
