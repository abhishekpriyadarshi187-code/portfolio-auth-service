package com.abhishek.portfolio.auth.service;

import com.abhishek.portfolio.auth.dto.AuthResponse;
import com.abhishek.portfolio.auth.dto.LoginRequest;
import com.abhishek.portfolio.auth.dto.RegisterRequest;
import com.abhishek.portfolio.auth.dto.RegisterResponse;

public interface AuthService {

    public RegisterResponse register(RegisterRequest registerRequest);

    public AuthResponse login(LoginRequest request);
}
