package com.akeshya.service;

import com.akeshya.entity.User;
import com.akeshya.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with email: " + email)
                );

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())       // Email used as principal
                .password(user.getPassword())
                .authorities(
                        user.getRoles()
                            .stream()
                            .map(role -> role.getName())
                            .toArray(String[]::new)
                )
                .disabled(!user.getEnabled())
                .build();
    }
}
