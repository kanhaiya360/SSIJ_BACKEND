package com.akeshya.service;

import org.springframework.http.ResponseEntity;

import com.akeshya.dto.request.UpdateCustomerRequest;

public interface CustomerService {
    ResponseEntity<?> getCustomerDetails();
    ResponseEntity<?> updateCustomerDetails(UpdateCustomerRequest request);

    ResponseEntity<?> deleteCustomer();
	ResponseEntity<?> getAllCustomers();
}