package com.abhishek.portfolio.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Email or mobile number cannot be blank")
    private String identifier;

    @NotBlank(message = "Password cannot be blank")
    private String password;
}