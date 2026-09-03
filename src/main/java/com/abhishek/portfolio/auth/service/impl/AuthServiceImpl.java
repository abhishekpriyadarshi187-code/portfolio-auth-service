package com.abhishek.portfolio.auth.service.impl;

import com.abhishek.portfolio.auth.dto.AuthResponse;
import com.abhishek.portfolio.auth.dto.LoginRequest;
import com.abhishek.portfolio.auth.dto.RegisterRequest;
import com.abhishek.portfolio.auth.dto.RegisterResponse;
import com.abhishek.portfolio.auth.exception.BaseException;
import com.abhishek.portfolio.auth.exception.InvalidCredentialsException;
import com.abhishek.portfolio.auth.exception.UserAlreadyExistsException;
import com.abhishek.portfolio.auth.exception.UserNotFoundException;
import com.abhishek.portfolio.auth.model.User;
import com.abhishek.portfolio.auth.repository.UserRepository;
import com.abhishek.portfolio.auth.security.JwtUtil;
import com.abhishek.portfolio.auth.service.AuthService;
import com.abhishek.portfolio.auth.service.SequenceGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_USER = "USER";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SequenceGeneratorService sequenceGeneratorService;
    private final JwtUtil jwtUtil;

    @Override
    public RegisterResponse register(final RegisterRequest registerRequest) {

        final String email = normalizeEmail(registerRequest.getEmail());
        final String mobileNumber =
                normalizeMobileNumber(registerRequest.getMobileNumber());

        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException(email);
        }

        if (userRepository.existsByMobileNumber(mobileNumber)) {
            throw new UserAlreadyExistsException(mobileNumber);
        }

        final String role =
                userRepository.count() == 0 ? ROLE_ADMIN : ROLE_USER;

        final long sequence =
                sequenceGeneratorService.generateSequence("user_sequence");

        final String userId =
                String.format("USR_%06d", sequence);

        final LocalDateTime now = LocalDateTime.now();

        User user = User.builder()
                .userId(userId)
                .email(email)
                .mobileNumber(mobileNumber)
                .fullName(registerRequest.getFullName().trim())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(role)
                .enabled(true)
                .accountNonLocked(true)
                .emailVerified(false)
                .mobileNumberVerified(false)
                .twoFactorEnabled(false)
                .createdAt(now)
                .updatedAt(now)
                .build();

        User savedUser = userRepository.save(user);

        return RegisterResponse.builder()
                .message("User registered successfully")
                .userId(savedUser.getUserId())
                .email(savedUser.getEmail())
                .mobileNumber(savedUser.getMobileNumber())
                .role(savedUser.getRole())
                .fullName(savedUser.getFullName())
                .build();
    }

    @Override
    public AuthResponse login(final LoginRequest request) {

        final String identifier = request.getIdentifier().trim();

        final User user;

        if (identifier.contains("@")) {

            final String email = normalizeEmail(identifier);

            user = userRepository.findByEmail(email)
                    .orElseThrow(() ->
                            new UserNotFoundException(identifier));

        } else {

            final String mobileNumber =
                    normalizeMobileNumber(identifier);

            user = userRepository.findByMobileNumber(mobileNumber)
                    .orElseThrow(() ->
                            new UserNotFoundException(identifier));
        }

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )) {
            throw new InvalidCredentialsException();
        }

        if (!user.isEnabled()) {
            throw new BaseException(
                    "Account is disabled",
                    HttpStatus.FORBIDDEN,
                    "ACCOUNT_DISABLED"
            ) {};
        }

        String token = jwtUtil.generateToken(
                user.getUserId(),
                user.getEmail(),
                user.getMobileNumber(),
                user.getRole()
        );

        return AuthResponse.builder()
                .token(token)
                .requiresTwoFactor(user.isTwoFactorEnabled())
                .role(user.getRole())
                .userId(user.getUserId())
                .build();
    }

    private String normalizeEmail(final String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeMobileNumber(final String mobileNumber) {
        return mobileNumber
                .trim()
                .replace(" ", "")
                .replace("-", "");
    }
}