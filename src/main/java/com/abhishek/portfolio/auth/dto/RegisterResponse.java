package com.abhishek.portfolio.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterResponse {
    private String userId;
    private String email;
    private String fullName;
    private String role;
    private String message;
    private String mobileNumber;
}
