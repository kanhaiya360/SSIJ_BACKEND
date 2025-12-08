package com.akeshya.service;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import com.akeshya.dto.request.LoginRequest;
import com.akeshya.dto.request.PasswordResetConfirmRequest;
import com.akeshya.dto.request.PasswordResetRequest;
import com.akeshya.dto.request.SignupRequest;
import com.akeshya.dto.request.VerifyOtpRequest;

public interface AuthService {

	ResponseEntity<?> login(LoginRequest request);

	ResponseEntity<?> register(SignupRequest request);

	ResponseEntity<?> verifyOtp(VerifyOtpRequest request);

}
