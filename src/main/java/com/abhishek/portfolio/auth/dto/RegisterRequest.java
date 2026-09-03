package com.abhishek.portfolio.auth.dto;

import com.abhishek.portfolio.auth.validation.StrongPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Mobile number cannot be empty")
    @Pattern(
            regexp = "^\\+[1-9]\\d{7,14}$",
            message = "Mobile number must be in international format, for example +919876543210"
    )
    private String mobileNumber;

    @NotBlank(message = "Full name cannot be empty")
    @Size(
            min = 2,
            max = 100,
            message = "Full name must be between 2 and 100 characters"
    )
    private String fullName;

    @StrongPassword
    private String password;
}
