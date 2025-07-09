package com.example.matching_fit.domain.user.service;

import com.example.matching_fit.domain.user.entity.User;
import com.example.matching_fit.global.security.ut.JwtUt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class AuthTokenService {
    @Value("${custom.jwt.secretKey}")
    private String jwtSecretKey;

    @Value("${custom.accessToken.expirationSeconds}")
    private long accessTokenExpirationSeconds;

    String genAccessToken(User user) {
        long id = user.getId();
        String email = user.getEmail();

        String name = user.getName();
        return JwtUt.jwt.toString(
                jwtSecretKey,
                accessTokenExpirationSeconds,
                Map.of("id", id, "email", email, "name", name)
        );
    }

    Map<String, Object> payload(String accessToken) {
        Map<String, Object> parsedPayload = JwtUt.jwt.payload(jwtSecretKey, accessToken);

        if (parsedPayload == null) return null;

        long id = (long) (Integer) parsedPayload.get("id");
        String email = (String) parsedPayload.get("email");
        String name = (String) parsedPayload.get("name");
        return Map.of("id", id, "email", email, "name", name);
    }
}
