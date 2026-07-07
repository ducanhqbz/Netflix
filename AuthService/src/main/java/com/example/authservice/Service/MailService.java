package com.example.authservice.Service;

import com.example.authservice.Service.Interface.IEmailService;
import com.example.authservice.config.RabbitMQConfig;
import com.example.authservice.entity.SendingMessage;
import jakarta.persistence.EntityManager;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MailService implements IEmailService {

    private RedisTemplate<String, String> redisTemplate;
    private RabbitTemplate rabbitTemplate;

    private EntityManager entityManager;

    public MailService(RedisTemplate<String, String> redisTemplate, RabbitTemplate rabbitTemplate, EntityManager entityManager) {
        this.redisTemplate = redisTemplate;
        this.rabbitTemplate = rabbitTemplate;
        this.entityManager = entityManager;
    }


    @Async
    @Override
    public void sendEmail(String to, String subject, String body) {
        SendingMessage sendingMessage = SendingMessage.builder()
                .address(to)
                .subject(subject)
                .contentText(body)
                .status(100)
                .build();
        entityManager.persist(sendingMessage);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EMAIL_EXCHANGE, "email", sendingMessage);

        entityManager.flush();
    }
}
