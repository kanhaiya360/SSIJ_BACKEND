package com.akeshya.dto.response;

import java.util.List;

public record JwtResponse(
        String token,
        String type,
        Long id,
        String contactNumber,
        String companyName,
        String branchName,
        String email,
        List<String> roles
) {
    public JwtResponse(String token, Long id, String contactNumber, String companyName,
                       String branchName, String email, List<String> roles) {
        this(token, "Bearer", id, contactNumber, companyName, branchName, email, roles);
    }
}
