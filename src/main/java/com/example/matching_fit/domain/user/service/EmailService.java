package com.example.matching_fit.domain.user.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Slf4j
@RequiredArgsConstructor
@Service
public class EmailService {
    private final JavaMailSender mailSender;

    private final RedisTemplate<String, String> redisTemplate;

//    @Transactional
//    public void sendVerificationEmail(String email) {
//        try {
//            String code = generateRandomCode();
//            storeCodeInRedis(email, code);
//
//            SimpleMailMessage message = new SimpleMailMessage();
//            message.setTo(email);
//            message.setSubject("이메일 인증 코드입니다.");
//            message.setText("인증 코드: " + code);
//
//            mailSender.send(message);
//
//            log.info("이메일 전송 성공: {}", email); // 성공 메시지
//        } catch (Exception e) {
//            log.error("이메일 전송 실패: {}", email, e); // 예외 발생 시 에러 메시지 출력
//            throw new RuntimeException("이메일 전송 중 오류가 발생했습니다.", e); // 예외 던지기
//        }
//    }

    @Transactional
    public void sendVerificationEmail(String email) {
        try {
            String code = generateRandomCode();
            storeCodeInRedis(email, code);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(email);
            helper.setFrom("matchingfit0000@gmail.com", "매칭핏"); // 여기서 이름 지정
            helper.setSubject("이메일 인증 코드입니다.");
            helper.setText("인증 코드: " + code, false);

            mailSender.send(mimeMessage);

            log.info("이메일 전송 성공: {}", email);
        } catch (Exception e) {
            log.error("이메일 전송 실패: {}", email, e);
            throw new RuntimeException("이메일 전송 중 오류가 발생했습니다.", e);
        }
    }


    private void storeCodeInRedis(String email, String code) {
        redisTemplate.opsForValue().set(email, code, Duration.ofMinutes(3));
    }

    @Transactional
    public boolean verifyCode(String email, String code) {
        String savedCode = redisTemplate.opsForValue().get(email);
        return code.equals(savedCode);
    }

    public String generateRandomCode() {
        return String.valueOf((int)(Math.random() * 899999) + 100000); // 6자리 숫자
    }

    public void sendResumeMatchedEmail(String toEmail, String resumeName, String companyName, String managerName) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setFrom("taeyun30504@gmail.com", "매칭핏"); // 보낸 사람 이름과 이메일
            helper.setSubject("[MatchingFit] 이력서 매칭 알림");
            helper.setText(
                    "회원님의 이력서 \"" + resumeName + "\"가 기업 \"" + companyName + "\"의 인사 담당자 \"" + managerName + "\"와 매칭되었습니다. 지금 확인해보세요!",
                    false // HTML 사용 안 함
            );

            mailSender.send(mimeMessage);
        } catch (Exception e) {
            throw new RuntimeException("이력서 매칭 알림 메일 발송 중 오류가 발생했습니다.", e);
        }
    }

}
