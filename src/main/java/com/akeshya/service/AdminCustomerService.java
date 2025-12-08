package com.akeshya.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.akeshya.entity.UserStatus;

public interface AdminCustomerService {
    ResponseEntity<?> deleteCustomer(UUID customerId);
    ResponseEntity<?> getCustomerById(UUID customerId);
    ResponseEntity<?> getAllCustomers();
    ResponseEntity<?> updateCustomerStatus(UUID customerId, UserStatus status);
}