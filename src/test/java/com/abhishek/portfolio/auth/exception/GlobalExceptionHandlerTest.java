package com.abhishek.portfolio.auth.exception;

import com.abhishek.portfolio.auth.dto.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleBaseException_shouldMapInvalidCredentialsToUnauthorized() {
        // Act
        ResponseEntity<ErrorResponse> response =
                handler.handleBaseException(new InvalidCredentialsException());

        // Assert
        assertErrorResponse(response, HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }

    @Test
    void handleBaseException_shouldMapDuplicateUserToBadRequest() {
        // Act
        ResponseEntity<ErrorResponse> response = handler.handleBaseException(
                new UserAlreadyExistsException("user@example.com")
        );

        // Assert
        assertErrorResponse(
                response,
                HttpStatus.BAD_REQUEST,
                "User already exists with email: user@example.com"
        );
    }

    @Test
    void handleBaseException_shouldMapMissingUserToNotFound() {
        // Act
        ResponseEntity<ErrorResponse> response = handler.handleBaseException(
                new UserNotFoundException("missing@example.com")
        );

        // Assert
        assertErrorResponse(
                response,
                HttpStatus.NOT_FOUND,
                "User not found with email: missing@example.com"
        );
    }

    @Test
    void handleValidationException_shouldReturnFirstValidationMessage() {
        // Arrange
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("registerRequest", "email", "Invalid email format"),
                new FieldError("registerRequest", "password", "Invalid password")
        ));

        // Act
        ResponseEntity<ErrorResponse> response = handler.handleValidationException(exception);

        // Assert
        assertErrorResponse(response, HttpStatus.BAD_REQUEST, "Invalid email format");
    }

    @Test
    void handleGenericException_shouldHideInternalErrorDetails() {
        // Act
        ResponseEntity<ErrorResponse> response =
                handler.handleGenericException(new IllegalStateException("sensitive detail"));

        // Assert
        assertErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong");
        assertThat(response.getBody().getMessage()).doesNotContain("sensitive detail");
    }

    private void assertErrorResponse(
            ResponseEntity<ErrorResponse> response,
            HttpStatus expectedStatus,
            String expectedMessage
    ) {
        assertThat(response.getStatusCode()).isEqualTo(expectedStatus);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(expectedStatus.value());
        assertThat(response.getBody().getMessage()).isEqualTo(expectedMessage);
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }
}
