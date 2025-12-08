package com.akeshya.bootstrap;

import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.akeshya.entity.Role;
import com.akeshya.entity.User;
import com.akeshya.entity.UserStatus;
import com.akeshya.repository.RoleRepository;
import com.akeshya.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        // ─────────────────── CREATE ROLES ───────────────────
        Role roleAdmin = createRoleIfNotExists("ROLE_ADMIN");
        Role roleUser = createRoleIfNotExists("ROLE_USER");

        // ─────────────────── CREATE DEFAULT ADMIN USER ───────────────────
        if (userRepository.count() == 0) {

            User admin = User.builder()
                    .contactNumber("9999999999")
                    .password(passwordEncoder.encode("admin123"))
                    .companyName("Akeshiya")
                    .branchName("Main Branch")
                    .shippingAddress("Hyderabad")
                    .contactPersonName("Super Admin")
                    .email("officialkanhaiya121@gmail.com")
                    .status(UserStatus.APPROVED)
                    .enabled(true)
                    .roles(Set.of(roleAdmin))   // Assign admin role only
                    .build();

            userRepository.save(admin);

            System.out.println("★★★★★ DEFAULT ADMIN CREATED ★★★★★");
        }
    }

    private Role createRoleIfNotExists(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(roleName);
                    return roleRepository.save(role);
                });
//    	System.out.println("Its working fine");
    }
}
