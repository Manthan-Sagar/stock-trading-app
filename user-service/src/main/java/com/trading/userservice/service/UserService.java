package com.trading.userservice.service;

import org.springframework.stereotype.Service;

import com.trading.userservice.dto.AuthResponse;
import com.trading.userservice.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Service 
@Slf4j 
public class UserService {
    
    private final UserRepository userRepository;

    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getEmail());

        if(userRepository.existsByEmail(request.getEmail())) {
            
        }
    }

    public AuthResponse login(LoginRequest request) {
        
    }
}
