package com.akeshya.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateCustomerRequest(
    @Size(max = 100, message = "Company name must be less than 100 characters")
    String companyName,
    
    @Size(max = 100, message = "Branch name must be less than 100 characters")
    String branchName,
    
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must be less than 100 characters")
    String email,
    
    @Size(max = 15, message = "GST number must be less than 15 characters")
    String gstNumber,
    
    @Size(max = 500, message = "Shipping address must be less than 500 characters")
    String shippingAddress,
    
    @Size(max = 100, message = "Contact person name must be less than 100 characters")
    String contactPersonName,
    
    List<@Size(max = 15, message = "Phone number must be less than 15 characters") String> additionalPhoneNumbers
) {}