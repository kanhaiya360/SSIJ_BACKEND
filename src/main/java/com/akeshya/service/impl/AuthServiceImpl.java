package com.akeshya.service.impl;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.akeshya.dto.JwtResponse;
import com.akeshya.dto.request.LoginRequest;
import com.akeshya.dto.request.SignupRequest;
import com.akeshya.dto.request.VerifyOtpRequest;
import com.akeshya.entity.Role;
import com.akeshya.entity.User;
import com.akeshya.entity.UserStatus;
import com.akeshya.repository.RoleRepository;
import com.akeshya.repository.UserRepository;
import com.akeshya.service.AuthService;
import com.akeshya.service.EmailService;
import com.akeshya.util.JwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    // SMS Gateway Center Configuration
    @Value("${sms.provider:console}")
    private String smsProvider;

    @Value("${sms.smsgatewaycenter.username}")
    private String sgUsername;

    @Value("${sms.smsgatewaycenter.password}")
    private String sgPassword;

    @Value("${sms.smsgatewaycenter.base-url}")
    private String sgBaseUrl;

    @Value("${sms.smsgatewaycenter.sender-id:AKESHY}")
    private String sgSenderId;

    @Value("${sms.smsgatewaycenter.route:TR}")
    private String sgRoute;

    @Value("${sms.smsgatewaycenter.type:TEXT}")
    private String sgType;

    @Value("${sms.enabled:true}")
    private boolean smsEnabled;

    @Value("${otp.expiry-minutes:5}")
    private int otpExpiryMinutes;

    @Value("${otp.rate-limit-minutes:1}")
    private int rateLimitMinutes;

    @Override
    public ResponseEntity<?> login(LoginRequest request) {

        // 1. Email + Password Login
        if (request.getEmail() != null && request.getPassword() != null) {
            return loginWithEmailPassword(request);
        }

        // 2. Email + OTP Login
        if (request.getEmail() != null ) {
        	User user = userRepository.findByEmail(request.getEmail()).orElseThrow( () -> new RuntimeException("User Not Found"));
            String otp=generateOtp();
            int otpInt=Integer.parseInt(otp);
        	String message= emailService.sendEmail(request.getEmail(),"OTP Verification Code ",otp);
        	user.setOtp(otpInt);
        	user.setOtpTimer(LocalDateTime.now().plusMinutes(10));
        	userRepository.save(user);
            return ResponseEntity.ok(message);
        }

        // 3. Contact Number + OTP Login
        if (request.getContactNumber() != null) {
            return loginWithMobileOtp(request.getContactNumber());
        }

        return ResponseEntity.badRequest().body(Map.of(
                "error", "Invalid login request",
                "message", "Provide (email password) or (email otp) or (contactNumber otp)"
        ));
    }


    private ResponseEntity<?> loginWithMobileOtp(String mobile) {

//        var otpEntity = otpRepository.findByContactNumberAndOtp(mobile, otp)
//                .orElseThrow(() -> new RuntimeException("Invalid OTP"));
//
//        if (otpEntity.getExpiry().isBefore(LocalDateTime.now())) {
//            return ResponseEntity.badRequest().body(Map.of("error", "OTP expired"));
//        }
//
//        User user = userRepository.findByContactNumber(mobile)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        if (!user.getEnabled()) {
//            return ResponseEntity.badRequest().body(Map.of(
//                    "error", "Account pending approval"
//            ));
//        }
//
//        String token = jwtUtil.generateTokenFromEmail(user.getEmail());
//
//        return ResponseEntity.ok(new JwtResponse(
//                token,
//                user.getId(),
//                user.getContactNumber(),
//                user.getEmail(),
//                user.getCompanyName(),
//                user.getBranchName(),
//                user.getRoles().stream().map(Role::getName).toList()
//        ));
    	return ResponseEntity.ok("feature coming soon...");
    }

	private ResponseEntity<?> loginWithEmailPassword(LoginRequest request) {
        try {
            Authentication auth = authenticate(request);
            SecurityContextHolder.getContext().setAuthentication(auth);

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!user.getEnabled()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Account pending approval",
                        "message", "Your account is waiting for admin approval"
                ));
            }


            String token = jwtUtil.generateJwtToken(auth);

            return ResponseEntity.ok(new JwtResponse(
                    token,
                    user.getId(),
                    user.getContactNumber(),
                    user.getEmail(),
                    user.getCompanyName(),
                    user.getBranchName(),
                    user.getRoles().stream().map(Role::getName).toList()
            ));

        } catch (BadCredentialsException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid email or password"));
        } catch (Exception e) {
            logger.error("Login error: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Login failed"));
        }
    }



	
    public Authentication authenticate(LoginRequest request) {
        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), request.getPassword()
                )
        );
    }
       
    
    @Override
    public ResponseEntity<?> verifyOtp(VerifyOtpRequest request) {
    	try {
            User user = null;

            // 1️⃣ Identify user by Email
            if (request.getEmail() != null && !request.getEmail().isEmpty()) {
                user = userRepository.findByEmail(request.getEmail())
                        .orElseThrow(() -> new RuntimeException("User not found"));
            }

            // 2️⃣ Identify user by Mobile
            else if (request.getContactNumber() != null && !request.getContactNumber().isEmpty()) {
                user = userRepository.findByContactNumber(request.getContactNumber())
                        .orElseThrow(() -> new RuntimeException("User not found"));
            }

            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email or Mobile is required"));
            }

            // 3️⃣ Verify OTP
//            int inputOtp = Integer.parseInt(request.getOtp());
            Integer inputOtp = Integer.valueOf(request.getOtp());

            if (user.getOtp() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "OTP not generated"));
            }

            if (!user.getOtp().equals(inputOtp)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid OTP"));
            }

            if (user.getOtpTimer().isBefore(LocalDateTime.now())) {
                return ResponseEntity.badRequest().body(Map.of("error", "OTP expired"));
            }

            // 4️⃣ OTP verified → clear the OTP
            user.setOtp(null);
            user.setOtpTimer(null);
            userRepository.save(user);

            // 5️⃣ Generate JWT Token
            String token = jwtUtil.generateTokenFromEmail(user.getEmail());

            return ResponseEntity.ok(new JwtResponse(
                    token,
                    user.getId(),
                    user.getContactNumber(),
                    user.getEmail(),
                    user.getCompanyName(),
                    user.getBranchName(),
                    user.getRoles().stream().map(Role::getName).toList()
            ));

        } catch (Exception e) {
            logger.error("OTP Verification Error: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", "OTP verification failed"));
        }
    }


    private Authentication authenticate(VerifyOtpRequest request) {
		return  authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), request.getOtp()
                )
        );
	}


	private String generateOtp() {
        return String.valueOf(new Random().nextInt(900000) + 100000);
    }


     @Override
    @Transactional
    public ResponseEntity<?> register(SignupRequest req) {
        try {
            if (userRepository.existsByContactNumber(req.contactNumber()))
                return ResponseEntity.badRequest().body(Map.of("error", "Contact number already registered"));

            if (req.email() != null && userRepository.existsByEmail(req.email()))
                return ResponseEntity.badRequest().body(Map.of("error", "Email already registered"));

            User user = User.builder()
                    .contactNumber(req.contactNumber())
                    .password(passwordEncoder.encode(req.password()))
                    .email(req.email())
                    .companyName(req.companyName())
                    .branchName(req.branchName())
                    .gstNumber(req.gstNumber())
                    .shippingAddress(req.shippingAddress())
                    .contactPersonName(req.contactPersonName())
                    .additionalPhoneNumbers(req.additionalPhoneNumbers())
                    .enabled(false)
                    .status(UserStatus.PENDING)
                    .build();

            Role role = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("ROLE_USER not found"));
            user.setRoles(Set.of(role));

            userRepository.save(user);

            logger.info("New user registered: {} - {}", req.contactNumber(), req.companyName());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "User registered successfully, pending admin approval"
            ));

        } catch (Exception e) {
            logger.error("Registration error: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Registration failed"
            ));
        }
    }

 
}