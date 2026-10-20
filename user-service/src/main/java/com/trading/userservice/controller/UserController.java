package com.trading.userservice.controller;

import java.math.BigDecimal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trading.userservice.dto.AuthResponse;
import com.trading.userservice.dto.LoginRequest;
import com.trading.userservice.dto.RegisterRequest;
import com.trading.userservice.dto.UserResponse;
import com.trading.userservice.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;



@RestController
@RequestMapping("/api/v1/users")
@Slf4j 
@RequiredArgsConstructor 
public class UserController {
    private final UserService userService;

    
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
        @Valid @RequestBody RegisterRequest request
    ){
        return  ResponseEntity.status(HttpStatus.CREATED)
            .body(userService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
        @Valid @RequestBody LoginRequest request
    ){
        return  ResponseEntity.status(HttpStatus.OK)
            .body(userService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile(
        @RequestHeader("X-User-Id") String userId
    ) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }
    

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }
    
    @PostMapping("/{userId}/funds/add")
    public ResponseEntity<UserResponse> addFunds(
        @PathVariable String userId,
        @RequestBody BigDecimal amount
    ) {
        return ResponseEntity.ok(userService.addFunds(userId, amount));
    }
    

    @PostMapping("/{userId}/funds/deduct")
    public ResponseEntity<UserResponse> deductFunds(
        @PathVariable String userId,
        @RequestBody BigDecimal amount
    ) {
        return ResponseEntity.ok(userService.deductFunds(userId, amount));
    }
    

    @PostMapping("/{userId}/funds/credit")
    public ResponseEntity<UserResponse> creditFunds(
        @PathVariable String userId,
        @RequestBody BigDecimal amount
    ) {
        return ResponseEntity.ok(userService.creditFunds(userId, amount));


    }


    
    

















}
