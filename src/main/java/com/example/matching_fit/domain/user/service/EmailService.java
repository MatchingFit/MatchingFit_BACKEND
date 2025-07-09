package com.example.matching_fit.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@RequiredArgsConstructor
@Service
public class EmailService {
    private final JavaMailSender mailSender;

    private final RedisTemplate<String, String> redisTemplate;


    public void sendVerificationEmail(String email) {
        try {
            String code = generateRandomCode();
            storeCodeInRedis(email, code);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("이메일 인증 코드입니다.");
            message.setText("인증 코드: " + code);

            mailSender.send(message);

            log.info("이메일 전송 성공: {}", email); // 성공 메시지
        } catch (Exception e) {
            log.error("이메일 전송 실패: {}", email, e); // 예외 발생 시 에러 메시지 출력
            throw new RuntimeException("이메일 전송 중 오류가 발생했습니다.", e); // 예외 던지기
        }
    }

    private void storeCodeInRedis(String email, String code) {
        redisTemplate.opsForValue().set(email, code, Duration.ofMinutes(3));
    }

    public boolean verifyCode(String email, String code) {
        String savedCode = redisTemplate.opsForValue().get(email);
        return code.equals(savedCode);
    }

    public String generateRandomCode() {
        return String.valueOf((int)(Math.random() * 899999) + 100000); // 6자리 숫자
    }
}
