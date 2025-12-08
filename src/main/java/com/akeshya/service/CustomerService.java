package com.akeshya.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.akeshya.dto.request.UpdateCustomerRequest;

public interface CustomerService {
    ResponseEntity<?> getCustomerDetails();
    ResponseEntity<?> updateCustomerDetails(UpdateCustomerRequest request);

    ResponseEntity<?> deleteCustomer();
	ResponseEntity<?> getAllCustomers();
	ResponseEntity<?> getCustomerById(UUID id);
}
