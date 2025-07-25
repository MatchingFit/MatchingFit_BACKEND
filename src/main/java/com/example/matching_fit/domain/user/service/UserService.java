package com.example.matching_fit.domain.user.service;

import com.example.matching_fit.domain.user.dto.ManagerJoinRequestDto;
import com.example.matching_fit.domain.user.dto.UserJoinRequestDto;
import com.example.matching_fit.domain.user.entity.User;
import com.example.matching_fit.domain.user.enums.LoginType;
import com.example.matching_fit.domain.user.enums.Role;
import com.example.matching_fit.domain.user.repository.UserRepository;
import com.example.matching_fit.global.security.rq.Rq;
import com.example.matching_fit.global.security.ut.JwtUt;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.*;

@RequiredArgsConstructor
@Service
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenService authTokenService;
    private final Rq rq;

    @Value("${custom.jwt.secretKey}")
    private String secretKey;
    public Optional<User> findByRefreshToken(String refreshToken) {
        return userRepository.findByRefreshToken(refreshToken);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    public Optional<User> findById(long authorId) {
        return userRepository.findById(authorId);
    }

    public String genAccessToken(User user) {
        return authTokenService.genAccessToken(user);
    }

    public User getUserFromAccessToken(String accessToken) {
        Map<String, Object> payload = authTokenService.payload(accessToken);
        if (payload == null) return null;

        long id = (long) payload.get("id");
        String email = (String) payload.get("email");
        String nickname = (String) payload.get("nickname");

        User user = new User(id, email, nickname);

        return user;
    }

    @Transactional
    public User join(UserJoinRequestDto userJoinRequestDto, LoginType loginType) {
        String email = userJoinRequestDto.getEmail();
        String password = userJoinRequestDto.getPassword();
        String passwordConfirm = userJoinRequestDto.getPasswordConfirm(); // 추가
        String name = userJoinRequestDto.getName();

        validateEmailDuplicate(email);

        // ✅ 영문+숫자 조합 & 10자리 이상 검사
        if (!isValidPassword(password)) {
            throw new IllegalArgumentException("비밀번호는 영문과 숫자를 조합해 10자리 이상이어야 합니다.");
        }


        // 비밀번호 & 비밀번호 확인 일치 여부 검사
        if (StringUtils.hasText(password) && !password.equals(passwordConfirm)) {
            throw new IllegalArgumentException("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        // 비밀번호 암호화 (소셜 로그인 시 비밀번호가 없을 수도 있음)
        if (StringUtils.hasText(password)) {
            password = passwordEncoder.encode(password);
        }

        User user = User.builder()
                .email(email)
                .password(password)
                .name(name)
                .loginType(loginType)
                .refreshToken(UUID.randomUUID().toString())
                .build();

        return userRepository.save(user);
    }

    @Transactional
    public User managerJoin(ManagerJoinRequestDto managerJoinRequestDto, LoginType loginType) {
        String email = managerJoinRequestDto.getEmail();
        String password = managerJoinRequestDto.getPassword();
        String passwordConfirm = managerJoinRequestDto.getPasswordConfirm(); // 추가
        String name = managerJoinRequestDto.getName();
        String companyName = managerJoinRequestDto.getCompanyName();

        validateEmailDuplicate(email);

        // ✅ 영문+숫자 조합 & 10자리 이상 검사
        if (!isValidPassword(password)) {
            throw new IllegalArgumentException("비밀번호는 영문과 숫자를 조합해 10자리 이상이어야 합니다.");
        }


        // 비밀번호 & 비밀번호 확인 일치 여부 검사
        if (StringUtils.hasText(password) && !password.equals(passwordConfirm)) {
            throw new IllegalArgumentException("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
        }

        // 비밀번호 암호화 (소셜 로그인 시 비밀번호가 없을 수도 있음)
        if (StringUtils.hasText(password)) {
            password = passwordEncoder.encode(password);
        }

        User user = User.builder()
                .email(email)
                .password(password)
                .name(name)
                .loginType(loginType)
                .companyName(companyName)
                .refreshToken(UUID.randomUUID().toString())
                .build();

        return userRepository.save(user);
    }

    @Transactional
    public void validateEmailDuplicate(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 다른 사용자가 사용중인 email 입니다.");
        }
    }

    private boolean isValidPassword(String password) {
        // 영문/숫자/특수문자 중 2가지 이상, 10자 이상
        String passwordPattern = "^(?=.*[A-Za-z].*)(?=.*\\d.*|.*[!@#$%^&*()\\-_=+\\[\\]{};:'\",.<>/?`~].*).{10,}$"
                + "|^(?=.*\\d.*)(?=.*[!@#$%^&*()\\-_=+\\[\\]{};:'\",.<>/?`~].*).{10,}$"
                + "|^(?=.*[A-Za-z].*)(?=.*[!@#$%^&*()\\-_=+\\[\\]{};:'\",.<>/?`~].*).{10,}$";
        return password.matches(passwordPattern);
    }


    @Transactional
    public String login(String email, String password) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                return rq.makeAuthCookie(user);
            }
        }
        throw new BadCredentialsException("유효하지 않은 이메일 또는 비밀번호입니다.");
    }

    @Transactional
    public void modify(User member, @NotBlank String nickname, String kakaoId) {
        member.setName(nickname);
        member.setKakaoId(kakaoId);
    }

    @Transactional
    public User kakaoJoin(String name, String email, LoginType provider, String kakaoId) {
        try {
            log.info("OAuth2 회원가입 처리 시작 - email: {}, name: {}, provider: {}", email, name, provider);

            // 사용자명 중복 체크
            userRepository.findByName(name).ifPresent(member -> {
                log.warn("OAuth2 회원가입 실패 - 이미 존재하는 사용자명: {}", name);
                throw new RuntimeException("해당 username은 이미 사용중입니다.");
            });

            // User 생성 - 엔티티 필드에 맞게 단일 Role만 설정
            User member = User.builder()
                    .name(name)
                    .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .email(email)
                    .refreshToken(UUID.randomUUID().toString())
                    .role(Role.USER)   // Set<Role> 아님, 단일 Role 필드
                    .loginType(provider)
                    .kakaoId(kakaoId)
                    .build();

            User savedUser = userRepository.save(member);
            log.info("OAuth2 회원가입 성공 - userId: {}, email: {}", savedUser.getId(), savedUser.getEmail());

            return savedUser;
        } catch (Exception e) {
            log.error("OAuth2 회원가입 처리 중 오류 발생", e);
            throw e;
        }
    }



    @Transactional
    public User modifyOrJoin(String username, String nickname, String kakaoId) {
        Optional<User> opMember = findByEmail(username);

        if (opMember.isPresent()) {
            User member = opMember.get();
            modify(member, nickname, kakaoId);
            return member;
        }

        return kakaoJoin(nickname, username, LoginType.KAKAO, kakaoId);
    }

    @Transactional
    public void updateRefreshToken(User user) {
        // 이미 user 객체에 refreshToken이 세팅된 상태이므로
        // DB에 반영하기 위해 save 또는 update 호출
        userRepository.save(user);
    }

    // 이메일과 비밀번호로 사용자 인증 (예시)
    public User authenticate(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

        // 비밀번호 검증 (예: BCrypt)
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }
        return user;
    }


    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }






}

