package com.example.authservice.controller;

import com.example.authservice.Service.AuthService;
import com.example.authservice.Service.JWTService;
import com.example.authservice.dto.RegisterRequest;
import com.example.authservice.dto.RegisterResponse;
import com.example.authservice.entity.User;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Role;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    private final JWTService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final AuthService authService;

    @GetMapping("/hello")
    @PreAuthorize("hasRole('USER')")
    public String sayHello() {
        return "Chào bạn! Đây là phản hồi từ AuthService.";
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String password = request.get("password");

            // Authenticate user
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            // Load user details
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Generate token
            String token = jwtService.generateToken(userDetails);

            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "username", username,
                    "message", "Đăng nhập thành công"
            ));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(401).body(Map.of(
                    "error", "Tên đăng nhập hoặc mật khẩu không chính xác"
            ));
        } catch (Exception ex) {
            return ResponseEntity.status(400).body(Map.of(
                    "error", "Lỗi: " + ex.getMessage()
            ));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            // Validate request
            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Username không được để trống"
                ));
            }
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Email không được để trống"
                ));
            }
            if (request.getPassword() == null || request.getPassword().length() < 6) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Password phải có ít nhất 6 ký tự"
                ));
            }
            if (!request.getPassword().equals(request.getConfirmPassword())) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Password và Confirm Password không khớp"
                ));
            }
            String error = authService.registerUser(request);
            if (!error.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", error));
            }

            RegisterResponse response = RegisterResponse.builder()
                    .status("SUCCESS")
                    .message("Đăng ký thành công! Bạn có thể đăng nhập ngay bây giờ.")
                    .build();

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", ex.getMessage()
            ));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Lỗi server: " + ex.getMessage()
            ));
        }
    }
}