package com.akeshya.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.akeshya.dto.request.LoginRequest;
import com.akeshya.dto.request.PasswordResetConfirmRequest;
import com.akeshya.dto.request.PasswordResetRequest;
import com.akeshya.dto.request.SignupRequest;
import com.akeshya.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin("*")
public class AuthController {

    private final AuthService authService;

    // -------- EMAIL / OTP LOGIN --------
    @PostMapping("/signin")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    // -------- REGISTER --------
    @PostMapping("/signup")
    public ResponseEntity<?> register(@Valid @RequestBody SignupRequest request) {
        return authService.register(request);
    }

    // -------- SEND OTP --------
    @PostMapping("/send-otp/{contactNumber}")
    public ResponseEntity<?> sendOtp(@PathVariable String contactNumber) {
        return authService.sendOtp(contactNumber);
    }

    // -------- VERIFY OTP --------
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(
            @RequestParam String contactNumber,
            @RequestParam String otp
    ) {
        return authService.verifyOtp(contactNumber, otp);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        return authService.getLoggedInUser();
    }
    
    @PostMapping("/password-reset")
    public ResponseEntity<?> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        return authService.requestPasswordReset(request);
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<?> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
        return authService.confirmPasswordReset(request);
    }

}
