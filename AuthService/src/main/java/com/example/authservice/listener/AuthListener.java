package com.example.authservice.listener;

//import com.example.authservice.config.RabbitMQConfig;

import com.example.authservice.Service.AuthService;
import com.example.authservice.Service.RoleService;
import com.example.authservice.config.RabbitMQConfig;
import com.example.authservice.dto.RegisterRequest;
import com.example.authservice.entity.Role;
import com.example.authservice.entity.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class AuthListener {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RoleService roleService;

    @Transactional
    @RabbitListener(
            queues = "auth-register-queue",
            concurrency = "3"
    )
    public void receiveRegister(RegisterRequest request) {
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .status("ACTIVE")
                .createdDate(LocalDateTime.now())
                .modifiedDate(LocalDateTime.now())
                .build();
        try {
            Role userRole = roleService.findByRoleCode("USER")
                    .orElseThrow(() -> new RuntimeException("Default USER role not found"));
            user.getRoles().add(userRole);
        } catch (Exception e) {
            // If role not found, just save without role (will be added later)
            System.out.println("Warning: Could not assign USER role: " + e.getMessage());
        }
        em.persist(user);

    }

    @RabbitListener(
            queues = "auth-login-queue",
            concurrency = "3"
    )
    public void receiveLogin(String message) {

        System.out.println(
                "[LOGIN] "
                        + Thread.currentThread().getName()
                        + " -> "
                        + message
        );

        // xử lý đăng nhập
    }

    @RabbitListener(
            queues = "auth-forgot-password-queue",
            concurrency = "3"
    )
    public void receiveForgotPassword(String message) {

        System.out.println(
                "[FORGOT PASSWORD] "
                        + Thread.currentThread().getName()
                        + " -> "
                        + message
        );

        // xử lý quên mật khẩu
    }


    @RabbitListener(
            queues = RabbitMQConfig.EMAIL_QUEUE,
            concurrency = "3"
    )
    public void receiveEmail(String emailsendingID) {


    }

}