package com.akeshya.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Random;

@RestController
@RequestMapping("/api/test")
public class TestMailController {

    @Autowired
    private JavaMailSender mailSender;

    @GetMapping("/send")
    public String sendOtp() {
        try {
            // Generate 6-digit OTP
            int otp = new Random().nextInt(900000) + 100000;

            // Prepare email
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo("dharukanhaiya@gmail.com");
            message.setSubject("Your OTP Code");
            message.setText("Your OTP is: " + otp);

            // ❗ IMPORTANT: Use VERIFIED SENDER (from Brevo → Senders & Domains)
            message.setFrom("kanhaiya.r.dharu@gmail.com");


            // Send email
            mailSender.send(message);

            return "OTP sent successfully → " + otp;

        } catch (Exception e) {
            e.printStackTrace();
            return "Failed: " + e.getMessage();
        }
    }
}
