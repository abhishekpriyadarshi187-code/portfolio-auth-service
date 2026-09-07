package com.abhishek.portfolio.auth.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class StrongPasswordValidatorTest {

    private final StrongPasswordValidator validator = new StrongPasswordValidator();

    @Test
    void isValid_shouldReturnTrue_whenPasswordSatisfiesEveryRule() {
        // Arrange
        String password = "Valid-test1!";

        // Act
        boolean valid = validator.isValid(password, null);

        // Assert
        assertThat(valid).isTrue();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidPasswords")
    void isValid_shouldReturnFalse_whenPasswordViolatesRule(
            String description,
            String password
    ) {
        // Act
        boolean valid = validator.isValid(password, null);

        // Assert
        assertThat(valid).as(description).isFalse();
    }

    private static Stream<Arguments> invalidPasswords() {
        return Stream.of(
                Arguments.of("null password", null),
                Arguments.of("blank password", "   "),
                Arguments.of("password shorter than eight characters", "Aa1!abc"),
                Arguments.of("password longer than 128 characters", "Aa1!" + "x".repeat(125)),
                Arguments.of("password without uppercase letter", "lowercase1!"),
                Arguments.of("password without lowercase letter", "UPPERCASE1!"),
                Arguments.of("password without number", "NoNumber!"),
                Arguments.of("password without special character", "NoSpecial1"),
                Arguments.of("password containing a space", "Has Space1!"),
                Arguments.of("password containing a tab", "Has\tTab1!")
        );
    }
}
