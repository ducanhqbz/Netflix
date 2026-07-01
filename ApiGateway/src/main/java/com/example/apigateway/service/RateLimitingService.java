package com.example.apigateway.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
@Service
@RequiredArgsConstructor
public class RateLimitingService {

    private final RedisTemplate<String, Long> redisTemplate;

    private static final long REQUESTS_PER_MINUTE = 3;

    public boolean isRequestAllowed(String userId) {

        String key = "rate_limit:" + userId;

        Long count = redisTemplate.opsForValue().get(key);

        if (count == null) {
            redisTemplate.opsForValue()
                    .set(key, 1L, 300, TimeUnit.SECONDS);
            return true;
        }

        if (count >= REQUESTS_PER_MINUTE) {
            return false;
        }

        redisTemplate.opsForValue().increment(key);

        return true;
    }

    public long getRemainingRequests(String userId) {

        Long count = redisTemplate.opsForValue()
                .get("rate_limit:" + userId);

        if (count == null) {
            return REQUESTS_PER_MINUTE;
        }

        return Math.max(0, REQUESTS_PER_MINUTE - count);
    }

    public void resetRateLimit(String userId) {
        redisTemplate.delete("rate_limit:" + userId);
    }
}