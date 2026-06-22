package com.example.authservice.listener;

//import com.example.authservice.config.RabbitMQConfig;

import com.example.authservice.config.RabbitMQConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class AuthListener {

    @PersistenceContext
    private EntityManager em;


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


    @RabbitListener(
            queues = RabbitMQConfig.EMail_QUEUE,
            concurrency = "3"
    )
    public void receiveEmail(String emailsendingID) {


    }

}