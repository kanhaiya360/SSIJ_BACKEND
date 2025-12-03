package com.akeshya.dto.request;

public record PasswordResetRequest(
	    String email,          
	    String contactNumber  
	) {}