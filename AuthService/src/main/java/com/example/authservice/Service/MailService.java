package com.example.authservice.Service;

import com.example.authservice.Service.Interface.IEmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class MailService implements IEmailService {

    private RedisTemplate<String, String> redisTemplate;

    public MailService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override

    public void sendEmail(String to, String subject, String body) {

    }
}
