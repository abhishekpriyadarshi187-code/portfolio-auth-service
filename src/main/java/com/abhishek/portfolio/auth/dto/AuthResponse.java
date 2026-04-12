package com.abhishek.portfolio.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token;

    private boolean requiresTwoFactor;

    private String role;

    private String userId;
}