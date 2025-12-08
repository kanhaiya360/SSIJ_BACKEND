package com.akeshya.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.annotation.Nullable;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Null;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true, length = 10)
    private String contactNumber;   // Used as username for login

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false)
    private String branchName;

    @Column(length = 15)
    private String gstNumber;      // optional

    @Column(nullable = false)
    private String shippingAddress;

    private String contactPersonName;  // optional

    @Column(unique = true)
    private String email;             // optional

    @ElementCollection
    @CollectionTable(
            name = "user_additional_numbers",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Column(name = "phone_number")
    private List<String> additionalPhoneNumbers;  // optional

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.PENDING;
    private Boolean enabled = true;
    
	 @CreationTimestamp
	    @Column(name = "created_date", updatable = false)
	    private LocalDateTime createdDate;

	    @UpdateTimestamp
	    @Column(name = "updated_date",insertable = false)
	    private LocalDateTime updatedDate;
	    
	    @Column(nullable = true)
	    private Integer otp;

	    @Column(nullable = true)
	    private LocalDateTime otpTimer;
}
