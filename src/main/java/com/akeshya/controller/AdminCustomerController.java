package com.akeshya.controller;

import com.akeshya.entity.UserStatus;
import com.akeshya.service.AdminCustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/customers")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class AdminCustomerController {

    private final AdminCustomerService adminCustomerService;

    // Delete customer by UUID (Admin only)
    @DeleteMapping("/{customerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteCustomer(@PathVariable UUID customerId) {
        return adminCustomerService.deleteCustomer(customerId);
    }

    @GetMapping("/{customerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getCustomerById(@PathVariable UUID customerId) {
        return adminCustomerService.getCustomerById(customerId);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllCustomers() {
        return adminCustomerService.getAllCustomers();
    }
    
    @PutMapping("/{customerId}/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateCustomerStatus(
            @PathVariable UUID customerId,
            @PathVariable UserStatus status) {
        return adminCustomerService.updateCustomerStatus(customerId, status);
    }
    
    
    
}