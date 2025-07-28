package com.example.matching_fit.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;
    private final long REFRESH_TOKEN_EXPIRATION = 60 * 60 * 24 * 7L; // 7일 (초)

    public void saveRefreshToken(String refreshToken, Long userId) {
        redisTemplate.opsForValue().set(refreshToken, userId.toString(), REFRESH_TOKEN_EXPIRATION, TimeUnit.SECONDS);
    }

    public String getUserIdByRefreshToken(String refreshToken) {
        return redisTemplate.opsForValue().get(refreshToken);
    }

    public void deleteRefreshToken(String refreshToken) {
        redisTemplate.delete(refreshToken);
    }

    public void saveAccessToken(String accessToken, Long userId, long expiration) {
        redisTemplate.opsForValue().set(accessToken, userId.toString(), expiration, TimeUnit.MILLISECONDS);
    }

    public boolean isAccessTokenValid(String accessToken) {
        return redisTemplate.hasKey(accessToken);
    }

    public void deleteAccessToken(String accessToken) {
        redisTemplate.delete(accessToken);
    }


}