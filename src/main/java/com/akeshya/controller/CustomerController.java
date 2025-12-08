
package com.akeshya.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.akeshya.dto.request.UpdateCustomerRequest;
import com.akeshya.service.CustomerService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/customer")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    // 1.3 Get Customer Details
    @GetMapping("/profile")
//    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getCustomerDetails() {
        return customerService.getCustomerDetails();
    }

    // 1.4 Update Customer Details
    @PutMapping("/profile")
//    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> updateCustomerDetails(@Valid @RequestBody UpdateCustomerRequest request) {
        return customerService.updateCustomerDetails(request);
    }

    // Change Password
 

    // 1.5 Delete Customer
    @DeleteMapping("/profile")
//    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> deleteCustomer() {
        return customerService.deleteCustomer();
    }
    
    @GetMapping("/all-users")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllCustomers(){
    	return customerService.getAllCustomers();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getCustomerById(@PathVariable UUID id) {
        return customerService.getCustomerById(id);
    }
    
}
