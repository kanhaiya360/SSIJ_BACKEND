package com.akeshya.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.akeshya.dto.request.LoginRequest;
import com.akeshya.dto.request.PasswordResetConfirmRequest;
import com.akeshya.dto.request.PasswordResetRequest;
import com.akeshya.dto.request.SignupRequest;
import com.akeshya.dto.request.VerifyOtpRequest;
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

	// -------- VERIFY OTP --------
	@PostMapping("/verify-otp")
	public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpRequest request) {
		return authService.verifyOtp(request);
	}

}
