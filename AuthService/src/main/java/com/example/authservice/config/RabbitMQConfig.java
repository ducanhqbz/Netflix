package com.example.authservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitMQConfig {

    public static final String AUTH_EXCHANGE = "auth-exchange";
    public static final String EMAIL_EXCHANGE = "email-exchange";

    public static final String AUTH_REGISTER_QUEUE = "auth-register-queue";
    public static final String AUTH_LOGIN_QUEUE = "auth-login-queue";
    public static final String AUTH_FORGOT_QUEUE = "auth-forgot-password-queue";

    @Bean
    public Queue registerQueue() {
        return new Queue(AUTH_REGISTER_QUEUE, true);
    }

    @Bean
    public Queue loginQueue() {
        return new Queue(AUTH_LOGIN_QUEUE, true);
    }

    @Bean
    public Queue forgotQueue() {
        return new Queue(AUTH_FORGOT_QUEUE, true);
    }

    @Bean
    public TopicExchange authExchange() {
        return new TopicExchange(AUTH_EXCHANGE);
    }

    @Bean
    public Binding registerBinding() {
        return BindingBuilder.bind(registerQueue())
                .to(authExchange())
                .with("auth.register");
    }

    @Bean
    public Binding loginBinding() {
        return BindingBuilder.bind(loginQueue())
                .to(authExchange())
                .with("auth.login");
    }

    @Bean
    public Binding forgotBinding() {
        return BindingBuilder.bind(forgotQueue())
                .to(authExchange())
                .with("auth.forgot");
    }
}