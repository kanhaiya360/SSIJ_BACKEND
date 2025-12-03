package com.akeshya.service;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import com.akeshya.dto.request.LoginRequest;
import com.akeshya.dto.request.PasswordResetConfirmRequest;
import com.akeshya.dto.request.PasswordResetRequest;
import com.akeshya.dto.request.SignupRequest;

public interface AuthService {

    ResponseEntity<?> login(LoginRequest request);

    Authentication authenticate(LoginRequest request);

    ResponseEntity<?> register(SignupRequest request);

    ResponseEntity<?> getLoggedInUser();

    ResponseEntity<?> sendOtp(String contactNumber);

    ResponseEntity<?> verifyOtp(String contactNumber, String otp);
    
 ResponseEntity<?> requestPasswordReset(PasswordResetRequest request);
    
    ResponseEntity<?> confirmPasswordReset(PasswordResetConfirmRequest request);
}
