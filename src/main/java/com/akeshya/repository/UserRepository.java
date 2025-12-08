package com.akeshya.repository;

import com.akeshya.entity.User;
import com.akeshya.entity.UserStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByContactNumber(String contactNumber);  
    Optional<User> findByEmail(String email);

    Boolean existsByContactNumber(String contactNumber);       
    Boolean existsByEmail(String email);
	List<User> findByStatus(UserStatus userStatus);
	Optional<User> findByEmailAndOtp(String email, String otp);

}
