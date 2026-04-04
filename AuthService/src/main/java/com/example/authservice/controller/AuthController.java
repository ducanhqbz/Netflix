package com.example.authservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth") // Khớp với Predicate Path=/api/auth/** ở Gateway
public class AuthController {

    @GetMapping("/hello")
    public String sayHello() {
        return "Chào bạn! Đây là phản hồi từ AuthService.";
    }
}