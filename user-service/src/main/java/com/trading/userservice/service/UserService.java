package com.trading.userservice.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.trading.userservice.dto.AuthResponse;
import com.trading.userservice.dto.LoginRequest;
import com.trading.userservice.dto.RegisterRequest;
import com.trading.userservice.dto.UserResponse;
import com.trading.userservice.entity.User;
import com.trading.userservice.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Service 
@Slf4j 
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder PasswordEncoder;
    private final KafkaTemplate<String,Object> kafkaTemplate;

    private static final String USER_REGISTERED_TOPIC = "user-registered";
    
    /**
     * Register a new user with the given request data.
     * @param request The user registration request containing user details.
     * @return An authentication response containing the new user's information and authentication tokens.
     */
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getEmail());

        if(userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException(
                "Email already registered " + request.getEmail()
            );
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(PasswordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setWalletBalance(
            request.getInitialDeposit() != null ? request.getInitialDeposit() : BigDecimal.valueOf(10000);
        );

        User savedUser = userRepository.save(user);
        log.info("user registered : {}",savedUser.getId());
        
        // publish user.registered event
        Map<String,Object> userRegisteredEvent = new HashMap<>();
        userRegisteredEvent.put("userId", savedUser.getId());
        userRegisteredEvent.put("email", savedUser.getEmail());
        userRegisteredEvent.put("firstName", savedUser.getFirstName());
        userRegisteredEvent.put("lastName", savedUser.getLastName());
        userRegisteredEvent.put("walletBalance", savedUser.getWalletBalance());


        kafkaTemplate.send(USER_REGISTERED_TOPIC, savedUser.getId(), userRegisteredEvent);
        log.info("Published user.registered event for user: {}", savedUser.getId());

        String token = generateAccessToken(savedUser.getId(),savedUser.getEmail());
        String refreshToken = generateRefreshToken(savedUser.getId(),savedUser.getEmail());

        return buildAuthResponse(savedUser,token,refreshToken);
        
    }

    /**
     * Login user with email and password
     * @param request
     * @return
     */

    public AuthResponse login(LoginRequest request){
        log.info("Logging in user: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("User not found"));

        if(!PasswordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        log.info("login successful for user: {}",user.getId());
        String token = generateAccessToken(user.getId(),user.getEmail());
        String refreshToken = generateRefreshToken(user.getId(),user.getEmail());

        return buildAuthResponse(user,token,refreshToken);
    }

    /**
     * Get User by id,
     * called by other services via feign(internal)
     * @param userId
     * @return
     */


    public UserResponse getUserById(String userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found" + userId));


        return mapToResponse(user);
    }


    private UserResponse mapToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setWalletBalance(user.getWalletBalance());
        response.setStatus(user.getStatus());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }



    private String generateAccessToken(String userId, String email) {
        return "access-token-" + userId;
    }

    private String generateRefreshToken(String userId, String email) {
        return "refresh-token-" + userId;
    }

    private AuthResponse buildAuthResponse(User user, String token, String refreshToken) {
        AuthResponse response = new AuthResponse();
        response.setAccessToken(token);
        response.setRefreshToken(refreshToken);
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setWalletBalance(user.getWalletBalance());
        return response;
    }

    /**
     * add funds to wallet - called on sell order failure or just topup
     * @param userId
     * @param amount
     * @return
     */
    public UserResponse addFunds(String userId, BigDecimal amount){
        log.info("Adding funds to user: {} with amount {}",userId,amount);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found" + userId));
        user.setWalletBalance(user.getWalletBalance().add(amount));
        User savedUser = userRepository.save(user);

        log.info("Funds added successfully for user: {}", savedUser.getId());

        return mapToResponse(user);
    }

    /**
     * deduct funds from wallet - called on buy order
     * @param userId
     * @param amount
     * @return
     */
    public UserResponse deductFunds(String userId, BigDecimal amount) {
        log.info("Deducting funds from user: {} with amount {}",userId,amount);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found" + userId));
        if(user.getWalletBalance().compareTo(amount)<0)
            throw new RuntimeException("Insufficient funds for user: " + userId);

        user.setWalletBalance(user.getWalletBalance().subtract(amount));
        User savedUser = userRepository.save(user);

        log.info("Funds deducted successfully for user: {}", savedUser.getId());

        return mapToResponse(user);
    }

    /**
     * credit funds to wallet - called on sell order
     * @param userId
     * @param amount
     * @return
     */

    public UserResponse creditFunds(String userId, BigDecimal amount) {
        log.info("Crediting funds to user: {} with amount {}",userId,amount);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found" + userId));
        user.setWalletBalance(user.getWalletBalance().add(amount));
        User savedUser = userRepository.save(user);

        log.info("Funds credited successfully for user: {}", savedUser.getId());

        return mapToResponse(user);
    }













    
}
