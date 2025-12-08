package com.akeshya.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetConfirmRequest(
    @NotBlank String token, 
    
    @NotBlank 
    @Size(min = 6, message = "Password must be at least 6 characters")
    String newPassword,
    
    String contactNumber    
) {}