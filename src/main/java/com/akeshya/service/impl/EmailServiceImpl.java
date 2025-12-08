package com.akeshya.service.impl;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import com.akeshya.service.EmailService;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public String sendEmail(String to, String subject, String otp) {
        try {

            ClassPathResource resource = new ClassPathResource("templates/email-template.html");

            String html = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);


            html = html.replace("{{OTP}}", otp);


            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom("kanhaiya.r.dharu@gmail.com");
            helper.setText(html, true); 
            mailSender.send(mimeMessage);

            return "HTML mail sent successfully to " + to;

        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to send email: " + e.getMessage();
        }
    }
}
