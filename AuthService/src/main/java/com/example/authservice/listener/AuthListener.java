package com.example.authservice.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class AuthListener {

    @RabbitListener(queues = "auth-register-queue")
    public void receiveRegister(String message) {
        System.out.println("Register: " + message);
    }

    @RabbitListener(queues = "auth-login-queue")
    public void receiveLogin(String message) {
        System.out.println("Login: " + message);
    }

    @RabbitListener(queues = "auth-forgot-password-queue")
    public void receiveForgotPassword(String message) {
        System.out.println("Forgot: " + message);
    }
}