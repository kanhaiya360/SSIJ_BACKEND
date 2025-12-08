package com.akeshya.dto.request;

import jakarta.validation.constraints.*;
import java.util.List;

public record SignupRequest(

    // Required fields
    @NotBlank(message = "Contact number is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian mobile number")
    String contactNumber,

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    String password,

    @NotBlank(message = "Company name is required")
    String companyName,

    @NotBlank(message = "Branch name is required")
    String branchName,

    @NotBlank(message = "Shipping address is required")
    String shippingAddress,

    // Optional fields
    @Pattern(
        regexp = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$",
        message = "Invalid GST number",
        flags = Pattern.Flag.CASE_INSENSITIVE
    )
    String gstNumber, // optional

    String contactPersonName, // optional

    @Email(message = "Invalid email")
    String email, // optional

    List<@Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid phone") String> additionalPhoneNumbers // optional

) {}
