package com.akeshya.dto.request;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;          // optional
    private String password;       // optional
    private String contactNumber;  // optional

}
