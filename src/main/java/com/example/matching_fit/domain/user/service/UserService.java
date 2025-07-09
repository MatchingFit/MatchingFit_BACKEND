package com.example.matching_fit.domain.user.service;

import com.example.matching_fit.domain.user.dto.UserJoinRequestDto;
import com.example.matching_fit.domain.user.entity.User;
import com.example.matching_fit.domain.user.enums.LoginType;
import com.example.matching_fit.domain.user.repository.UserRepository;
import com.example.matching_fit.global.security.rq.Rq;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenService authTokenService;
    private final Rq rq;
    public Optional<User> findByRefreshToken(String refreshToken) {
        return userRepository.findByRefreshToken(refreshToken);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByNickname(String nickname) {
        return userRepository.findByName(nickname);
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

        // 이메일 중복 검사
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 가입한 email 입니다.");
        }

        // 닉네임 중복 검사
        if (userRepository.existsByName(name)) {
            throw new IllegalArgumentException("이미 사용중인 nickname 입니다.");
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
    public String login(String email, String password) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                return rq.makeAuthCookie(user);
            }
        }
        throw new BadCredentialsException("Invalid email or password");
    }


    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }


}
