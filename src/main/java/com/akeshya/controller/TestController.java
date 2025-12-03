package com.akeshya.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@CrossOrigin("*")
public class TestController {

    // Exotel credentials
    private static final String EXOTEL_SID = "test6456";
    private static final String EXOTEL_API_KEY = "18f5188f882818874b904e627da8724794ee8c46e93d1772";
    private static final String EXOTEL_API_TOKEN = "e0040436a5aec680ab8fc3397573e317008aba4d6323a563";
    private static final String EXOTEL_SENDER_ID = "AKESHY";

    /**
     * SIMPLE SMS TEST - Just send a test message
     */
    @PostMapping("/sms")
    public ResponseEntity<?> testSms(@RequestParam String phone) {
        try {
            System.out.println("🔧 Testing SMS to: " + phone);
            
            String message = "Test from Akeshya - Hello!";
            String result = sendExotelSms(phone, message);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "phone", phone,
                "message", message,
                "result", result
            ));
            
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    /**
     * TEST OTP - Send OTP message
     */
    @PostMapping("/otp")
    public ResponseEntity<?> testOtp(@RequestParam String phone) {
        try {
            System.out.println("🔧 Testing OTP to: " + phone);
            
            String otp = "123456";
            String message = "Your OTP is " + otp + " - Akeshya";
            String result = sendExotelSms(phone, message);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "phone", phone,
                "otp", otp,
                "message", message,
                "result", result
            ));
            
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    /**
     * CHECK STATUS - Simple status check
     */
    @GetMapping("/status")
    public ResponseEntity<?> status() {
        return ResponseEntity.ok(Map.of(
            "status", "Running",
            "service", "Akeshya SMS Test",
            "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * EXOTEL SMS SENDING METHOD
     */
    private String sendExotelSms(String phone, String message) {
        try {
            String url = "https://api.exotel.com/v1/Accounts/" + EXOTEL_SID + "/Sms/send.json";
            
            // Prepare auth
            String auth = EXOTEL_API_KEY + ":" + EXOTEL_API_TOKEN;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            
            // Prepare data
            String formData = "From=" + EXOTEL_SENDER_ID + 
                            "&To=91" + phone + 
                            "&Body=" + java.net.URLEncoder.encode(message, "UTF-8");
            
            System.out.println("📤 Sending to: " + phone);
            System.out.println("📝 Message: " + message);
            
            // Send request
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Basic " + encodedAuth)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(formData))
                    .timeout(java.time.Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            System.out.println("📨 Response: " + response.statusCode() + " - " + response.body());
            
            return response.statusCode() + " - " + response.body();
            
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }
}