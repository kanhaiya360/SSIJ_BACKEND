package com.akeshya.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.hibernate.annotations.UpdateTimestamp;

import com.akeshya.entity.Role;
import com.akeshya.entity.UserStatus;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Userdto {
    private UUID id;
    private String contactNumber;
    private String companyName;
    private String branchName;
    private String gstNumber; 
    private String shippingAddress;
    private String contactPersonName;
    private String email;
    private List<String> additionalPhoneNumbers; 
    private Set<Role> roles;
    private UserStatus status;
    private Boolean enabled;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

}