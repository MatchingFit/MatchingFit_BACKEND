package com.example.matching_fit.domain.user.controller;

import com.example.matching_fit.domain.user.dto.AccessTokenResponse;
import com.example.matching_fit.domain.user.dto.RefreshTokenRequest;
import com.example.matching_fit.domain.user.entity.User;
import com.example.matching_fit.domain.user.service.AuthTokenService;
import com.example.matching_fit.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/token")
@RequiredArgsConstructor
public class TokenController {

    private final AuthTokenService authTokenService;
    private final UserService userService; // User 조회용

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(@RequestBody RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        // ✅ payload 복호화 ❌ → 대신 DB 조회!
        User user = userService.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("유효하지 않은 Refresh Token"));

        // 새 Access Token 발급
        String newAccessToken = authTokenService.genAccessToken(user);

        return ResponseEntity.ok(new AccessTokenResponse(newAccessToken));
    }
}
