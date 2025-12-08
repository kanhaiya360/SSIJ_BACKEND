package com.akeshya.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.akeshya.dto.response.CustomerResponse;
import com.akeshya.dto.response.Userdto;
import com.akeshya.entity.User;
import com.akeshya.entity.UserStatus;
import com.akeshya.repository.UserRepository;
import com.akeshya.service.AdminCustomerService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminCustomerServiceImpl implements AdminCustomerService {

    private final UserRepository userRepository;
    
    private static final Logger logger = LoggerFactory.getLogger(AdminCustomerServiceImpl.class);

    @Override
    public ResponseEntity<?> deleteCustomer(UUID customerId) {
        try {
            // Get current admin user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String adminUsername = auth.getName();
            
            logger.info("Admin {} attempting to delete customer with ID: {}", adminUsername, customerId);

            // Find the customer to delete
            User customer = userRepository.findById(customerId)
                    .orElseThrow(() -> {
                        logger.warn("Customer not found with ID: {}", customerId);
                        return new RuntimeException("Customer not found with ID: " + customerId);
                    });

            // Prevent admin from deleting themselves
            if (customer.getContactNumber().equals(adminUsername)) {
                logger.warn("Admin {} attempted to delete their own account", adminUsername);
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "You cannot delete your own account",
                        "error", "SELF_DELETION_NOT_ALLOWED"
                ));
            }

            // Store customer info for response before deletion
            String contactNumber = customer.getContactNumber();
            String companyName = customer.getCompanyName();
            String branchName = customer.getBranchName();

            // Delete the customer
            userRepository.delete(customer);

            logger.info("Admin {} successfully deleted customer: {} ({}) - {}", 
                    adminUsername, contactNumber, companyName, branchName);

            // Return success response
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Customer deleted successfully",
                    "deletedCustomer", Map.of(
                            "id", customerId,
                            "contactNumber", contactNumber,
                            "companyName", companyName,
                            "branchName", branchName
                    ),
                    "deletedBy", adminUsername,
                    "timestamp", java.time.LocalDateTime.now()
            ));

        } catch (RuntimeException e) {
            logger.error("Error deleting customer {}: {}", customerId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Failed to delete customer",
                    "error", e.getMessage()
            ));
        } catch (Exception e) {
            logger.error("Unexpected error deleting customer {}: {}", customerId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "An unexpected error occurred",
                    "error", "INTERNAL_SERVER_ERROR"
            ));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getCustomerById(UUID customerId) {
        try {
            logger.info("Fetching customer details for ID: {}", customerId);

            User customer = userRepository.findById(customerId)
                    .orElseThrow(() -> {
                        logger.warn("Customer not found with ID: {}", customerId);
                        return new RuntimeException("Customer not found with ID: " + customerId);
                    });

            var response = convertToUserDto(customer);

            logger.info("Successfully fetched customer: {} ({})", 
                    customer.getContactNumber(), customer.getCompanyName());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "customer", response
            ));

        } catch (RuntimeException e) {
            logger.error("Error fetching customer {}: {}", customerId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Failed to fetch customer",
                    "error", e.getMessage()
            ));
        } catch (Exception e) {
            logger.error("Unexpected error fetching customer {}: {}", customerId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "An unexpected error occurred",
                    "error", "INTERNAL_SERVER_ERROR"
            ));
        }
    }

    private Userdto convertToUserDto(User user) {
        return new Userdto(
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
        		user.getUpdatedDate());
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<?> getAllCustomers() {
        try {
            logger.info("Fetching all customers");

            List<User> customers = userRepository.findAll();

            List<Userdto> customerList = customers.stream()
                    .map(this::convertToUserDto)
                    .collect(Collectors.toList());

            logger.info("Successfully fetched {} customers", customerList.size());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "customers", customerList,
                    "total", customerList.size(),
                    "timestamp", java.time.LocalDateTime.now()
            ));

        } catch (Exception e) {
            logger.error("Error fetching all customers: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Failed to fetch customers",
                    "error", e.getMessage()
            ));
        }
    }

    @Override
    public ResponseEntity<?> updateCustomerStatus(UUID customerId, UserStatus status) {
        try {
            // Get current admin user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String adminUsername = auth.getName();
            
            logger.info("Admin {} updating status for customer ID: {} to {}", 
                    adminUsername, customerId, status);

            // Find the customer
            User customer = userRepository.findById(customerId)
                    .orElseThrow(() -> {
                        logger.warn("Customer not found with ID: {}", customerId);
                        return new RuntimeException("Customer not found with ID: " + customerId);
                    });

            // Store previous values
            UserStatus previousStatus = customer.getStatus();
            Boolean previousEnabled = customer.getEnabled();

            // Check if status is actually changing
            if (previousStatus == status) {
                logger.info("Customer {} already has status: {}", customerId, status);
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Customer already has status: " + status
                ));
            }

            // Update status
            customer.setStatus(status);
            
            // Auto-manage enabled flag
            if (status == UserStatus.APPROVED) {
                customer.setEnabled(true);
            } else if (status == UserStatus.REJECTED ) {
                customer.setEnabled(false);
            }

            userRepository.save(customer);

            logger.info("Successfully updated customer {}: {} -> {} (enabled: {})", 
                    customerId, previousStatus, status, customer.getEnabled());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Customer status updated successfully",
                    "customerId", customerId,
                    "contactNumber", customer.getContactNumber(),
                    "companyName", customer.getCompanyName(),
                    "previousStatus", previousStatus,
                    "newStatus", status,
                    "enabled", customer.getEnabled(),
                    "updatedBy", adminUsername,
                    "updatedAt", LocalDateTime.now()
            ));

        } catch (RuntimeException e) {
            logger.error("Error updating customer status {}: {}", customerId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Failed to update customer status",
                    "error", e.getMessage()
            ));
        } catch (Exception e) {
            logger.error("Unexpected error updating customer status {}: {}", customerId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "An unexpected error occurred",
                    "error", "INTERNAL_SERVER_ERROR"
            ));
        }
    }

}