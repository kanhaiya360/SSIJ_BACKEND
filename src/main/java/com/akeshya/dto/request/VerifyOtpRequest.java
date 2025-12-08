package com.akeshya.dto.request;

import lombok.Data;

@Data
public class VerifyOtpRequest {
	private String email;
	private String contactNumber;
	private  String otp;

}
