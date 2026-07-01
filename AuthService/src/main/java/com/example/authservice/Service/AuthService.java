package com.example.authservice.Service;

import com.example.authservice.Repository.UserRepository;
import com.example.authservice.dto.RegisterRequest;
import com.example.authservice.entity.Role;
import com.example.authservice.entity.User;
import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;

@Service
@AllArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;

    private final RabbitTemplate rabbitTemplate;

    /**
     * Register new user
     */
    public String registerUser(RegisterRequest request) {
        String error = "";
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            error = "Username cannot be empty";
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            error = "Email cannot be empty";
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            error = "Password must be at least 6 characters";
        }
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            error = "Password and confirm password do not match";
        }

        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            error = "Username already exists";
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            error = "Email already exists";
        }

        rabbitTemplate.convertAndSend("auth-exchange", "auth.register", request);

        return error;
    }

    /**
     * Check if username exists
     */
    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Check if email exists
     */
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Get current timestamp as String (YYYY-MM-DD HH:mm:ss)
     */
    private String getCurrentTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.now().format(formatter);
    }
}
