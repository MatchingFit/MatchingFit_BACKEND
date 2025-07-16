package com.example.matching_fit.domain.user.controller;

import com.example.matching_fit.domain.user.dto.AccessTokenResponse;
import com.example.matching_fit.domain.user.dto.RefreshTokenRequest;
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

        System.out.println("refreshToken 쿠키: " + refreshToken);

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

        // 새 Access Token 발급
        String newAccessToken = authTokenService.genAccessToken(user);

        return ResponseEntity.ok(ApiResponse.success(newAccessToken, "재발급 성공!"));
    }
}
