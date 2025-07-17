package com.example.matching_fit.domain.user.controller;

import com.example.matching_fit.domain.user.entity.User;
import com.example.matching_fit.domain.user.service.AuthTokenService;
import com.example.matching_fit.domain.user.service.UserService;
import com.example.matching_fit.global.rp.ApiResponse;
import com.example.matching_fit.global.security.rq.Rq;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/token")
@RequiredArgsConstructor
public class TokenController {

    private final AuthTokenService authTokenService;
    private final UserService userService; // User 조회용
    private final Rq rq;

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(HttpServletRequest request) {
        String refreshToken = null;

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("리프레시 토큰이 없습니다."));
        }

        // DB에서 유저 조회
        Optional<User> optionalUser = userService.findByRefreshToken(refreshToken);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("유효하지 않은 Refresh Token입니다."));
        }

        User user = optionalUser.get();

        // 🔍 Redis에서 기존 accessToken 있는지 확인
        Optional<String> redisAccessToken = authTokenService.getAccessTokenFromRedis(user.getId()); // 또는 user.getEmail()

        if (redisAccessToken.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success(redisAccessToken.get(), "기존 Access Token 반환"));
        }

        // 🆕 accessToken 새로 발급 + Redis 저장
        String newAccessToken = authTokenService.genAccessToken(user); // 내부에서 Redis 저장도 함께 한다면 좋음

        return ResponseEntity.ok(ApiResponse.success(newAccessToken, "Access Token 재발급 성공"));
    }

}
