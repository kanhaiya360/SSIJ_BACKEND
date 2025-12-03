package com.akeshya.dto;

import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class JwtResponse {

    private String token;
    private String type = "Bearer";

    private UUID id;
    private String contactNumber;
    private String email;
    private String companyName;
    private String branchName;
    private List<String> roles;

    public JwtResponse(String token, UUID id, String contactNumber, String email,
                       String companyName, String branchName, List<String> roles) {
        this.token = token;
        this.id = id;
        this.contactNumber = contactNumber;
        this.email = email;
        this.companyName = companyName;
        this.branchName = branchName;
        this.roles = roles;
    }
}
