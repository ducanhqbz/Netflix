package com.example.authservice.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class AuthListener {

    @RabbitListener(
            queues = "auth-register-queue",
            concurrency = "3"
    )
    public void receiveRegister(String message) {

        System.out.println(
                "[REGISTER] "
                        + Thread.currentThread().getName()
                        + " -> "
                        + message
        );

        // xử lý đăng ký
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
}