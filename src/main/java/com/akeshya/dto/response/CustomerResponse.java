package com.akeshya.dto.response;

import java.util.List;
import java.util.UUID;

public record CustomerResponse(
    UUID id,
    String contactNumber,
    String companyName,
    String branchName,
    String gstNumber,
    String shippingAddress,
    String contactPersonName,
    String email,
    List<String> additionalPhoneNumbers
  
) {}