package com.akeshya.dto.response;

import java.time.LocalDateTime;
import java.util.Set;

public record LoginResponse(
	    String token,
	    String type,            // "Bearer"
	    Long userId,
	    String companyName,
	    String contactNumber,
	    Set<String> roles,
	    LocalDateTime expiresAt
	) {}