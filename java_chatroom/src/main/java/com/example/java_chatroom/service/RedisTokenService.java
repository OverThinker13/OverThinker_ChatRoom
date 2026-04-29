package com.example.java_chatroom.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisTokenService {

    private static final String TOKEN_PREFIX = "token:user:";
    private static final long TOKEN_EXPIRE_HOURS = 24;

    private final StringRedisTemplate redisTemplate;

    public RedisTokenService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveToken(int userId, String token) {
        String key = TOKEN_PREFIX + userId;
        redisTemplate.opsForValue().set(key, token, TOKEN_EXPIRE_HOURS, TimeUnit.HOURS);
    }

    public boolean isValid(int userId, String token) {
        String key = TOKEN_PREFIX + userId;
        String storedToken = redisTemplate.opsForValue().get(key);
        return token.equals(storedToken);
    }

    public void removeToken(int userId) {
        String key = TOKEN_PREFIX + userId;
        redisTemplate.delete(key);
    }
}
