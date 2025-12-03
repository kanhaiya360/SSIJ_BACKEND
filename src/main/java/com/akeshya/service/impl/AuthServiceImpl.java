package com.akeshya.service.impl;

import java.util.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.akeshya.dto.JwtResponse;
import com.akeshya.dto.request.LoginRequest;
import com.akeshya.dto.request.PasswordResetConfirmRequest;
import com.akeshya.dto.request.PasswordResetRequest;
import com.akeshya.dto.request.SignupRequest;
import com.akeshya.entity.Role;
import com.akeshya.entity.User;
import com.akeshya.entity.UserStatus;
import com.akeshya.repository.RoleRepository;
import com.akeshya.repository.UserRepository;
import com.akeshya.service.AuthService;
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

    private final Map<String, String> otpStore = new HashMap<>();
    private final Map<String, Long> otpTimestamp = new HashMap<>();
    private final Map<String, Integer> otpAttempts = new HashMap<>();
    private final Map<String, String> resetTokens = new HashMap<>();        // token -> email/phone
    private final Map<String, Long> resetTokenTimestamp = new HashMap<>();  // token -> timestamp
    private static final long RESET_TOKEN_EXPIRY_MS = 30 * 60 * 1000; // 30 minutes

    // ============================
    // LOGIN METHODS (Keep your existing login methods)
    // ============================
    @Override
    public ResponseEntity<?> login(LoginRequest request) {
        // --- EMAIL LOGIN ---
        if (request.getEmail() != null && request.getPassword() != null) {
            return loginWithEmail(request);
        }

        // --- OTP LOGIN ---
        if (request.getContactNumber() != null && request.getOtp() != null) {
            return verifyOtp(request.getContactNumber(), request.getOtp());
        }

        return ResponseEntity.badRequest().body(Map.of(
                "error", "Invalid login request",
                "message", "Use either email/password or contactNumber/otp"
        ));
    }

    private ResponseEntity<?> loginWithEmail(LoginRequest request) {
        try {
            Authentication auth = authenticate(request);
            SecurityContextHolder.getContext().setAuthentication(auth);

            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Check if user is enabled and approved
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
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid email or password"
            ));
        } catch (Exception e) {
            logger.error("Login error: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Login failed"
            ));
        }
    }

    @Override
    public Authentication authenticate(LoginRequest request) {
        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), request.getPassword()
                )
        );
    }

    // ============================
    // OTP SEND & VERIFY with SMS Gateway Center
    // ============================
    @Override
    public ResponseEntity<?> sendOtp(String contactNumber) {
        try {
            // Validate contact number
            if (!isValidContactNumber(contactNumber)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid contact number",
                    "message", "Please provide a valid 10-digit Indian mobile number"
                ));
            }

            Optional<User> user = userRepository.findByContactNumber(contactNumber);
            if (user.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Contact number not registered",
                    "message", "This mobile number is not registered with us"
                ));
            }

            // Check if user account is approved
            if (!user.get().getEnabled()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Account pending approval",
                    "message", "Your account is waiting for admin approval"
                ));
            }

            // Check rate limiting
            if (isRateLimited(contactNumber)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Too many OTP requests",
                    "message", "Please wait " + rateLimitMinutes + " minute(s) before requesting another OTP"
                ));
            }

            // Generate OTP
            String otp = generateOtp();
            long currentTime = System.currentTimeMillis();

            // Store OTP with timestamp
            otpStore.put(contactNumber, otp);
            otpTimestamp.put(contactNumber, currentTime);
            otpAttempts.put(contactNumber, 0);

            // Send OTP via selected provider
            boolean smsSent = false;
            if (smsEnabled) {
                smsSent = sendOtpToUser(contactNumber, otp);
            }

            if (smsSent) {
                logger.info("OTP sent successfully to {} via {}", contactNumber, smsProvider.toUpperCase());
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "OTP sent successfully to your registered mobile number",
                    "contactNumber", contactNumber,"otp",otp,
                    "provider", smsProvider.toUpperCase()
                ));
            } else {
                // Fallback for development
                logger.info("SMS not sent. OTP for {}: {}", contactNumber, otp);
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "OTP generated successfully",
                    "contactNumber", contactNumber,
                    "otp", otp,
                    "provider", "CONSOLE",
                    "note", "SMS is disabled or failed"
                ));
            }

        } catch (Exception e) {
            logger.error("Error sending OTP to {}: {}", contactNumber, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Failed to send OTP",
                "message", "Please try again later"
            ));
        }
    }

    private boolean sendOtpToUser(String contactNumber, String otp) {
        switch (smsProvider.toLowerCase()) {
            case "smsgatewaycenter":
                return sendViaSmsGatewayCenter(contactNumber, otp);
            case "console":
            default:
                logger.info("📱 OTP for {}: {}", contactNumber, otp);
                return true;
        }
    }

    private boolean sendViaSmsGatewayCenter(String contactNumber, String otp) {
        try {
            // Prepare message
            String message = String.format(
                "Your OTP for Akeshya login is %s. Valid for %d minutes. - Akeshya Jewellers",
                otp, otpExpiryMinutes
            );

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(java.time.Duration.ofSeconds(10))
                    .build();

            // Build URL with parameters (SMS Gateway Center uses GET requests)
            String url = String.format("%s?UserName=%s&Password=%s&SenderId=%s&Message=%s&MobileNo=91%s&Route=%s&Type=%s",
                sgBaseUrl,
                java.net.URLEncoder.encode(sgUsername, "UTF-8"),
                java.net.URLEncoder.encode(sgPassword, "UTF-8"),
                java.net.URLEncoder.encode(sgSenderId, "UTF-8"),
                java.net.URLEncoder.encode(message, "UTF-8"),
                contactNumber,
                sgRoute,
                sgType
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .timeout(java.time.Duration.ofSeconds(15))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            logger.info("SMS Gateway Center Response: {}", response.body());

            // Check response - SMS Gateway Center returns simple response
            if (response.statusCode() == 200) {
                String responseBody = response.body().toLowerCase();
                
                // Check for success indicators
                boolean success = responseBody.contains("message submitted successfully") ||
                                 responseBody.contains("success") ||
                                 responseBody.contains("msgid") ||
                                 !responseBody.contains("error") ||
                                 !responseBody.contains("fail");

                if (success) {
                    logger.info("✅ SMS sent successfully via SMS Gateway Center to {}", contactNumber);
                    return true;
                } else {
                    logger.error("❌ SMS Gateway Center error: {}", response.body());
                    return false;
                }
            } else {
                logger.error("❌ SMS Gateway Center HTTP error: {} - {}", response.statusCode(), response.body());
                return false;
            }

        } catch (Exception e) {
            logger.error("❌ Error sending SMS via SMS Gateway Center to {}: {}", contactNumber, e.getMessage());
            return false;
        }
    }

    @Override
    public ResponseEntity<?> verifyOtp(String contactNumber, String otp) {
        try {
            // Validate inputs
            if (!isValidContactNumber(contactNumber)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid contact number"));
            }

            if (!isValidOtp(otp)) {
                return ResponseEntity.badRequest().body(Map.of("error", "OTP must be 6 digits"));
            }

            if (!otpStore.containsKey(contactNumber)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "No OTP requested",
                    "message", "Please request an OTP first"
                ));
            }

            // Check OTP expiration
            if (isOtpExpired(contactNumber)) {
                cleanupOtp(contactNumber);
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "OTP expired",
                    "message", "Please request a new OTP"
                ));
            }

            // Track attempts
            int attempts = otpAttempts.getOrDefault(contactNumber, 0) + 1;
            otpAttempts.put(contactNumber, attempts);

            if (attempts > 5) {
                cleanupOtp(contactNumber);
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Too many failed attempts",
                    "message", "Please request a new OTP"
                ));
            }

            if (!otpStore.get(contactNumber).equals(otp)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid OTP",
                    "attempts", attempts,
                    "message", "Invalid OTP. " + (5 - attempts) + " attempts remaining"
                ));
            }

            User user = userRepository.findByContactNumber(contactNumber)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Generate token
            String token = jwtUtil.generateTokenWithPhone(contactNumber);

            // Clean up OTP data
            cleanupOtp(contactNumber);

            logger.info("OTP login successful for user: {}", contactNumber);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "token", token,
                "user", Map.of(
                    "id", user.getId(),
                    "contactNumber", user.getContactNumber(),
                    "email", user.getEmail(),
                    "companyName", user.getCompanyName(),
                    "branchName", user.getBranchName(),
                    "roles", user.getRoles().stream().map(Role::getName).toList()
                ),
                "message", "Login successful"
            ));

        } catch (Exception e) {
            logger.error("Error verifying OTP for {}: {}", contactNumber, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "OTP verification failed"
            ));
        }
    }

    // ============================
    // UTILITY METHODS (Keep your existing utility methods)
    // ============================
    private boolean isValidContactNumber(String contactNumber) {
        return contactNumber != null && contactNumber.matches("^[6-9]\\d{9}$");
    }

    private boolean isValidOtp(String otp) {
        return otp != null && otp.matches("^\\d{6}$");
    }

    private String generateOtp() {
        return String.valueOf(new Random().nextInt(900000) + 100000);
    }

    private boolean isOtpExpired(String contactNumber) {
        Long timestamp = otpTimestamp.get(contactNumber);
        if (timestamp == null) return true;
        return (System.currentTimeMillis() - timestamp) > (otpExpiryMinutes * 60 * 1000L);
    }

    private boolean isRateLimited(String contactNumber) {
        Long timestamp = otpTimestamp.get(contactNumber);
        if (timestamp == null) return false;
        return (System.currentTimeMillis() - timestamp) < (rateLimitMinutes * 60 * 1000L);
    }

    private void cleanupOtp(String contactNumber) {
        otpStore.remove(contactNumber);
        otpTimestamp.remove(contactNumber);
        otpAttempts.remove(contactNumber);
    }

    // ============================
    // REGISTER (Keep your existing register method)
    // ============================
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

    // ============================
    // GET CURRENT USER (Keep your existing method)
    // ============================
    @Override
    public ResponseEntity<?> getLoggedInUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
            if (auth == null || !auth.isAuthenticated()) {
                return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
            }

            String username = auth.getName();
            User user;

            // Check if login was via email or phone
            if (username.contains("@")) {
                user = userRepository.findByEmail(username)
                        .orElseThrow(() -> new RuntimeException("User not found"));
            } else {
                user = userRepository.findByContactNumber(username)
                        .orElseThrow(() -> new RuntimeException("User not found"));
            }

            return ResponseEntity.ok(Map.of(
                    "user", Map.of(
                            "id", user.getId(),
                            "contactNumber", user.getContactNumber(),
                            "email", user.getEmail(),
                            "companyName", user.getCompanyName(),
                            "branchName", user.getBranchName(),
                            "roles", user.getRoles().stream().map(Role::getName).toList(),
                            "enabled", user.getEnabled(),
                            "status", user.getStatus()
                    )
            ));

        } catch (Exception e) {
            logger.error("Error fetching current user: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Unable to fetch user"
            ));
        }
    }

 // ============================
 // PASSWORD RESET FUNCTIONALITY
 // ============================
 @Override
 public ResponseEntity<?> requestPasswordReset(PasswordResetRequest request) {
     try {
         // Validate input - must provide either email or contactNumber
         if ((request.email() == null || request.email().trim().isEmpty()) && 
             (request.contactNumber() == null || request.contactNumber().trim().isEmpty())) {
             return ResponseEntity.badRequest().body(Map.of(
                 "error", "Email or contact number is required"
             ));
         }

         Optional<User> user = Optional.empty();

         // Find user by email
         if (request.email() != null && !request.email().trim().isEmpty()) {
             user = userRepository.findByEmail(request.email());
             if (user.isEmpty()) {
                 // Don't reveal that email doesn't exist for security
                 return ResponseEntity.ok(Map.of(
                     "success", true,
                     "message", "If the email exists, a password reset link has been sent"
                 ));
             }
         }
         // Find user by contact number
         else if (request.contactNumber() != null && !request.contactNumber().trim().isEmpty()) {
             if (!isValidContactNumber(request.contactNumber())) {
                 return ResponseEntity.badRequest().body(Map.of(
                     "error", "Invalid contact number"
                 ));
             }
             user = userRepository.findByContactNumber(request.contactNumber());
             if (user.isEmpty()) {
                 // Don't reveal that phone doesn't exist for security
                 return ResponseEntity.ok(Map.of(
                     "success", true,
                     "message", "If the phone number exists, an OTP has been sent"
                 ));
             }
         }

         if (user.isPresent()) {
             User foundUser = user.get();
             
             // Email-based reset
             if (request.email() != null && !request.email().trim().isEmpty()) {
                 return handleEmailPasswordReset(foundUser);
             } 
             // Phone-based reset
             else if (request.contactNumber() != null && !request.contactNumber().trim().isEmpty()) {
                 return handlePhonePasswordReset(foundUser);
             }
         }

         return ResponseEntity.internalServerError().body(Map.of(
             "error", "Password reset failed"
         ));

     } catch (Exception e) {
         logger.error("Password reset request error: {}", e.getMessage(), e);
         return ResponseEntity.internalServerError().body(Map.of(
             "error", "Password reset request failed"
         ));
     }
 }

 private ResponseEntity<?> handleEmailPasswordReset(User user) {
     try {
         // Generate reset token
         String resetToken = UUID.randomUUID().toString();
         long currentTime = System.currentTimeMillis();

         // Store token with user identifier
         resetTokens.put(resetToken, user.getEmail());
         resetTokenTimestamp.put(resetToken, currentTime);

         // TODO: Send email with reset link
         // In production, you would integrate with an email service
         // For now, we'll log the reset link
         
         String resetLink = "http://localhost:8080/api/auth/password-reset/confirm?token=" + resetToken;
         logger.info("📧 Password reset link for {}: {}", user.getEmail(), resetLink);

         return ResponseEntity.ok(Map.of(
             "success", true,
             "message", "Password reset instructions have been sent to your email",
             "resetToken", resetToken, // Only for development/testing
             "note", "In production, this token would be sent via email"
         ));

     } catch (Exception e) {
         logger.error("Email password reset error: {}", e.getMessage(), e);
         return ResponseEntity.internalServerError().body(Map.of(
             "error", "Failed to process password reset"
         ));
     }
 }

 private ResponseEntity<?> handlePhonePasswordReset(User user) {
     try {
         // Generate OTP for phone reset
         String otp = generateOtp();
         long currentTime = System.currentTimeMillis();

         // Store OTP for password reset (separate from login OTP)
         String resetOtpKey = "RESET_" + user.getContactNumber();
         otpStore.put(resetOtpKey, otp);
         otpTimestamp.put(resetOtpKey, currentTime);

         // Send OTP via SMS
         boolean smsSent = false;
         if (smsEnabled) {
             smsSent = sendPasswordResetOtp(user.getContactNumber(), otp);
         }

         if (smsSent) {
             logger.info("Password reset OTP sent to {}", user.getContactNumber(),otp);
             return ResponseEntity.ok(Map.of(
                 "success", true,
                 "message", "Password reset OTP has been sent to your mobile number",
                 "contactNumber", user.getContactNumber(),
                 "provider", smsProvider.toUpperCase()
             ));
         } else {
             // Fallback for development
             logger.info("Password reset OTP for {}: {}", user.getContactNumber(), otp);
             return ResponseEntity.ok(Map.of(
                 "success", true,
                 "message", "Password reset OTP generated",
                 "contactNumber", user.getContactNumber(),
                 "otp", otp,
                 "provider", "CONSOLE",
                 "note", "For development testing"
             ));
         }

     } catch (Exception e) {
         logger.error("Phone password reset error: {}", e.getMessage(), e);
         return ResponseEntity.internalServerError().body(Map.of(
             "error", "Failed to send password reset OTP"
         ));
     }
 }

 private boolean sendPasswordResetOtp(String contactNumber, String otp) {
     try {
         String message = String.format(
             "Your password reset OTP for Akeshya is %s. Valid for %d minutes. - Akeshya Jewellers",
             otp, otpExpiryMinutes
         );

         // Use your existing SMS sending method
         return sendViaSmsGatewayCenter(contactNumber, otp);

     } catch (Exception e) {
         logger.error("Error sending password reset OTP: {}", e.getMessage());
         return false;
     }
 }

 @Override
 public ResponseEntity<?> confirmPasswordReset(PasswordResetConfirmRequest request) {
     try {
         // Validate input
         if (request.token() == null || request.token().trim().isEmpty()) {
             return ResponseEntity.badRequest().body(Map.of(
                 "error", "Reset token is required"
             ));
         }

         if (request.newPassword() == null || request.newPassword().length() < 6) {
             return ResponseEntity.badRequest().body(Map.of(
                 "error", "Password must be at least 6 characters"
             ));
         }

         User user = null;
         String token = request.token();

         // Check if it's a phone OTP reset
         if (request.contactNumber() != null && !request.contactNumber().trim().isEmpty()) {
             user = handlePhonePasswordResetConfirm(request);
         } 
         // Check if it's an email token reset
         else {
             user = handleEmailPasswordResetConfirm(token);
         }

         if (user != null) {
             // Update password
             user.setPassword(passwordEncoder.encode(request.newPassword()));
             userRepository.save(user);

             // Cleanup reset data
             cleanupResetData(token, request.contactNumber());

             logger.info("Password reset successful for user: {}", user.getEmail() != null ? user.getEmail() : user.getContactNumber());

             return ResponseEntity.ok(Map.of(
                 "success", true,
                 "message", "Password has been reset successfully"
             ));
         } else {
             return ResponseEntity.badRequest().body(Map.of(
                 "error", "Invalid or expired reset token"
             ));
         }

     } catch (Exception e) {
         logger.error("Password reset confirmation error: {}", e.getMessage(), e);
         return ResponseEntity.internalServerError().body(Map.of(
             "error", "Password reset failed"
         ));
     }
 }

 private User handlePhonePasswordResetConfirm(PasswordResetConfirmRequest request) {
     String otpKey = "RESET_" + request.contactNumber();
     
     if (!otpStore.containsKey(otpKey)) {
         logger.error("No password reset OTP found for {}", request.contactNumber());
         return null;
     }

     // Check OTP expiration
     Long timestamp = otpTimestamp.get(otpKey);
     if (timestamp == null || (System.currentTimeMillis() - timestamp) > (otpExpiryMinutes * 60 * 1000L)) {
         cleanupResetData(null, request.contactNumber());
         logger.error("Password reset OTP expired for {}", request.contactNumber(),otpKey);
         return null;
     }

     if (!otpStore.get(otpKey).equals(request.token())) {
         logger.error("Invalid password reset OTP for {}", request.contactNumber());
         return null;
     }

     // Find user by contact number
     return userRepository.findByContactNumber(request.contactNumber())
             .orElse(null);
 }

 private User handleEmailPasswordResetConfirm(String token) {
     if (!resetTokens.containsKey(token)) {
         logger.error("Invalid password reset token");
         return null;
     }

     // Check token expiration
     Long timestamp = resetTokenTimestamp.get(token);
     if (timestamp == null || (System.currentTimeMillis() - timestamp) > RESET_TOKEN_EXPIRY_MS) {
         resetTokens.remove(token);
         resetTokenTimestamp.remove(token);
         logger.error("Password reset token expired");
         return null;
     }

     String userEmail = resetTokens.get(token);
     
     // Find user by email
     return userRepository.findByEmail(userEmail)
             .orElse(null);
 }

 private void cleanupResetData(String token, String contactNumber) {
     if (token != null) {
         resetTokens.remove(token);
         resetTokenTimestamp.remove(token);
     }
     if (contactNumber != null) {
         String otpKey = "RESET_" + contactNumber;
         otpStore.remove(otpKey);
         otpTimestamp.remove(otpKey);
     }
 }
}