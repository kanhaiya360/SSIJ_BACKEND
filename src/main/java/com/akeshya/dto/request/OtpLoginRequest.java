package com.akeshya.dto.request;

public record OtpLoginRequest(
        String contactNumber,
        String otp
) {}

