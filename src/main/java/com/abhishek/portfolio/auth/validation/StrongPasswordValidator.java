package com.abhishek.portfolio.auth.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class StrongPasswordValidator
        implements ConstraintValidator<StrongPassword, String> {

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 128;

    private static final Pattern UPPERCASE_PATTERN =
            Pattern.compile("[A-Z]");

    private static final Pattern LOWERCASE_PATTERN =
            Pattern.compile("[a-z]");

    private static final Pattern NUMBER_PATTERN =
            Pattern.compile("\\d");

    private static final Pattern SPECIAL_CHARACTER_PATTERN =
            Pattern.compile("[^A-Za-z0-9\\s]");

    private static final Pattern WHITESPACE_PATTERN =
            Pattern.compile("\\s");

    @Override
    public boolean isValid(
            String password,
            ConstraintValidatorContext context
    ) {
        if (password == null || password.isBlank()) {
            return false;
        }

        if (password.length() < MIN_LENGTH || password.length() > MAX_LENGTH) {
            return false;
        }

        if (WHITESPACE_PATTERN.matcher(password).find()) {
            return false;
        }

        return UPPERCASE_PATTERN.matcher(password).find()
                && LOWERCASE_PATTERN.matcher(password).find()
                && NUMBER_PATTERN.matcher(password).find()
                && SPECIAL_CHARACTER_PATTERN.matcher(password).find();
    }
}