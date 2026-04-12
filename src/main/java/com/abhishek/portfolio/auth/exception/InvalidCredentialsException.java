package com.abhishek.portfolio.auth.exception;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends BaseException {

    public InvalidCredentialsException() {
        super(
                "Invalid email or password",
                HttpStatus.UNAUTHORIZED,
                "INVALID_CREDENTIALS"
        );
    }
}
