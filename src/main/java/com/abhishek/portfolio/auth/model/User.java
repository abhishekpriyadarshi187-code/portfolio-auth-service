package com.abhishek.portfolio.auth.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    @Indexed(unique = true)
    private String mobileNumber;

    private String fullName;

    /**
     * BCrypt-encoded password.
     * Never store the raw password.
     */
    private String password;

    private String role;

    @Indexed(unique = true)
    private String userId;

    @Builder.Default
    private boolean enabled = true;

    @Builder.Default
    private boolean accountNonLocked = true;

    @Builder.Default
    private boolean emailVerified = false;

    @Builder.Default
    private boolean mobileNumberVerified = false;

    @Builder.Default
    private boolean twoFactorEnabled = false;

    private String twoFactorSecret;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
}