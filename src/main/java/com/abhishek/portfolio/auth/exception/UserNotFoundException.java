package com.abhishek.portfolio.auth.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends BaseException {

    public UserNotFoundException(String email) {
        super(
                "User not found with email: " + email,
                HttpStatus.NOT_FOUND,
                "USER_NOT_FOUND"
        );
    }
}
