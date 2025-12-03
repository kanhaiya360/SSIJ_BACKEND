package com.akeshya.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.akeshya.dto.request.UpdateCustomerRequest;
import com.akeshya.dto.response.CustomerResponse;
import com.akeshya.dto.response.Userdto;
import com.akeshya.entity.User;
import com.akeshya.repository.UserRepository;
import com.akeshya.service.CustomerService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    private static final Logger logger = LoggerFactory.getLogger(CustomerServiceImpl.class);

    // 1.3 Get Customer Details
    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getCustomerDetails() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String contactNumber = auth.getName();

            User user = userRepository.findByContactNumber(contactNumber)
                    .orElseThrow(() -> new RuntimeException("Customer not found"));

            // Create response DTO
            CustomerResponse response = new CustomerResponse(
            		user.getId(),
            		user.getContactNumber(),
            		user.getCompanyName(),
            		user.getBranchName(),
                    user.getGstNumber(),
                    user.getShippingAddress(),
                    user.getContactPersonName(),
                    user.getEmail(),
                    user.getAdditionalPhoneNumbers()
                   
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching customer details: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Failed to fetch customer details",
                    "error", e.getMessage()
            ));
        }
    }

    // 1.4 Update Customer Details
    @Override
    public ResponseEntity<?> updateCustomerDetails(UpdateCustomerRequest request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String contactNumber = auth.getName();

            User user = userRepository.findByContactNumber(contactNumber)
                    .orElseThrow(() -> new RuntimeException("Customer not found"));

            // Update fields if provided
            if (request.companyName() != null && !request.companyName().isBlank()) {
                user.setCompanyName(request.companyName());
            }
            if (request.branchName() != null && !request.branchName().isBlank()) {
                user.setBranchName(request.branchName());
            }
            if (request.email() != null && !request.email().isBlank()) {
                // Check if email is already taken by another user
                if (!request.email().equals(user.getEmail()) && 
                    userRepository.existsByEmail(request.email())) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "error", "Email already taken by another customer"
                    ));
                }
                user.setEmail(request.email());
            }
            if (request.gstNumber() != null) {
                user.setGstNumber(request.gstNumber());
            }
            if (request.shippingAddress() != null && !request.shippingAddress().isBlank()) {
                user.setShippingAddress(request.shippingAddress());
            }
            if (request.contactPersonName() != null) {
                user.setContactPersonName(request.contactPersonName());
            }
            if (request.additionalPhoneNumbers() != null) {
                user.setAdditionalPhoneNumbers(request.additionalPhoneNumbers());
            }

            User updatedUser = userRepository.save(user);

            // Return updated customer response
            CustomerResponse response = new CustomerResponse(
                    updatedUser.getId(),
                    updatedUser.getContactNumber(),
                    updatedUser.getCompanyName(),
                    updatedUser.getBranchName(),
                    updatedUser.getGstNumber(),
                    updatedUser.getShippingAddress(),
                    updatedUser.getContactPersonName(),
                    updatedUser.getEmail(),
                    updatedUser.getAdditionalPhoneNumbers()
                   
            );

            return ResponseEntity.ok(Map.of(
                    "message", "Customer details updated successfully",
                    "customer", response
            ));

        } catch (Exception e) {
            logger.error("Error updating customer details: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Failed to update customer details",
                    "error", e.getMessage()
            ));
        }
    }



    // 1.5 Delete Customer
    @Override
    public ResponseEntity<?> deleteCustomer() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String contactNumber = auth.getName();

            User user = userRepository.findByContactNumber(contactNumber)
                    .orElseThrow(() -> new RuntimeException("Customer not found"));

            userRepository.delete(user);

            // Clear security context
            SecurityContextHolder.clearContext();

            return ResponseEntity.ok(Map.of(
                    "message", "Customer account deleted successfully"
            ));

        } catch (Exception e) {
            logger.error("Error deleting customer: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Failed to delete customer account",
                    "error", e.getMessage()
            ));
        }
    }

    @Override
    public ResponseEntity<?> getAllCustomers() {
        List<User> users = userRepository.findAll();
        
       
        List<Userdto> userDtos = users.stream().filter(u -> u.getRoles().stream()
                .noneMatch(r -> r.getName().equals("ROLE_ADMIN")))
            .map(user -> new Userdto(
            		 user.getId(),
                     user.getContactNumber(),
                     user.getCompanyName(),
                     user.getBranchName(),
                     user.getGstNumber(),
                     user.getShippingAddress(),
                     user.getContactPersonName(),
                     user.getEmail(),
                     user.getAdditionalPhoneNumbers(),  
                     user.getRoles(),
                     user.getStatus(),
                     user.getEnabled(),
            		user.getCreatedDate(),
            		user.getUpdatedDate()
            ))
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(userDtos);
    }
}