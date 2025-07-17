package com.example.matching_fit.domain.user.controller;

import com.example.matching_fit.domain.user.dto.EmailVerificationDto;
import com.example.matching_fit.domain.user.dto.UserEmailDto;
import com.example.matching_fit.domain.user.service.EmailService;
import com.example.matching_fit.domain.user.service.UserService;
import com.example.matching_fit.global.rp.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/email")
@Tag(name = "Email-Verify", description = "이메일 인증 관련 API")
@Slf4j
public class EmailController {

    private final EmailService emailService;
    private final UserService userService;

    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email.matches(emailRegex);
    }

    @PostMapping("/join/send")
    public ResponseEntity<ApiResponse<?>> sendCodeByJoin(@RequestBody UserEmailDto userEmailDto) {
        String email = userEmailDto.getEmail();
        log.info("your email1 {}", email);

        // 이메일 형식 직접 검증
        if (!isValidEmail(email)) {
            return ResponseEntity.badRequest().body(ApiResponse.fail("유효한 이메일 형식이어야 합니다."));
        }

        if (userService.emailExists(email)) {
            return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.fail("이미 사용 중인 이메일입니다."));
        }

        emailService.sendVerificationEmail(email);
        return ResponseEntity.ok(ApiResponse.success(null, "이메일 전송 성공"));
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyCode(@RequestBody EmailVerificationDto emailVerificationDto) {
        boolean result = emailService.verifyCode(emailVerificationDto.getEmail(), emailVerificationDto.getCode());
        if (result) {
            return ResponseEntity.ok(ApiResponse.success(null, "인증 성공"));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail("인증 실패"));
        }
    }
}
