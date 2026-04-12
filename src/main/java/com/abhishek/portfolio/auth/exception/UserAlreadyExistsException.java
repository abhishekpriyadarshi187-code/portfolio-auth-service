package com.abhishek.portfolio.auth.exception;

import org.springframework.http.HttpStatus;

public class UserAlreadyExistsException extends BaseException {

    public UserAlreadyExistsException(String email) {
        super(
                "User already exists with email: " + email,
                HttpStatus.BAD_REQUEST,
                "USER_ALREADY_EXISTS"
        );
    }
}