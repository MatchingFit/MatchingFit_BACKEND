package com.example.matching_fit.domain.user.controller;

import com.example.matching_fit.domain.user.dto.*;
import com.example.matching_fit.domain.user.entity.User;
import com.example.matching_fit.domain.user.enums.LoginType;
import com.example.matching_fit.domain.user.service.AuthTokenService;
import com.example.matching_fit.domain.user.service.UserService;
import com.example.matching_fit.global.rp.ApiResponse;
import com.example.matching_fit.global.security.rq.Rq;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "user 관련 API")
@Slf4j
public class UserController {

    private final UserService userService;
    private final AuthTokenService authTokenService;
    private final Rq rq;

    @PostMapping("/join")
    public ResponseEntity<ApiResponse<String>> joinUser(@RequestBody UserJoinRequestDto userJoinRequestDto) {
        try {
            User join = userService.join(userJoinRequestDto, LoginType.LOCAL);
            ApiResponse<String> joinSuccess = ApiResponse.success(join.getEmail(), "회원가입 성공");
            return ResponseEntity.ok(joinSuccess);
        } catch (IllegalArgumentException e) {
            ApiResponse<String> errorResponse = ApiResponse.fail(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            ApiResponse<String> errorResponse = ApiResponse.fail("알 수 없는 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequest) {
        User user = userService.authenticate(loginRequest.getEmail(), loginRequest.getPassword());

        // 1) accessToken 발급 (JWT)
        String accessToken = authTokenService.genAccessToken(user);

        // 2) refreshToken 생성 (랜덤 UUID) 및 DB 저장
        String refreshToken = UUID.randomUUID().toString();
        user.setRefreshToken(refreshToken);
        userService.updateRefreshToken(user);

        // 3) refreshToken을 HttpOnly 쿠키로 설정
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true) // HTTPS 환경일 때 true로 설정하세요
                .path("/")    // 필요한 경로 설정
                .maxAge(60 * 60 * 24 * 7) // 7일 유효기간 (초 단위)
                .sameSite("Strict") // 혹은 "Lax"
                .build();

        // 4) 응답에 accessToken은 바디로, refreshToken은 쿠키로 담아 반환
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(new LoginResponseDto(user.getName(),accessToken));
    }

    @DeleteMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logoutUser() {
        rq.deleteCookie("accessToken");
        rq.deleteCookie("refreshToken");
//        userService.deleteUser();
        return ResponseEntity.ok(ApiResponse.success(null, "로그아웃 성공"));
    }

    @GetMapping("/check-email")
    public ResponseEntity<CheckEmailResponseDto> checkEmailDuplicate(@RequestParam String email) {
        try {
            userService.validateEmailDuplicate(email);
            // 중복 아님 → 사용 가능
            return ResponseEntity.ok(new CheckEmailResponseDto(
                    true,
                    false,
                    "사용 가능한 이메일입니다."
            ));
        } catch (IllegalArgumentException e) {
            // 중복 → 이미 가입됨
            return ResponseEntity.status(HttpStatus.OK).body(new CheckEmailResponseDto(
                    true,
                    true,
                    e.getMessage()
            ));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoDto>> getUserInfo() {
        User user = rq.getActor();
        if (user == null) {
            return ResponseEntity
                    .status(401) // Unauthorized
                    .body(ApiResponse.fail("사용자 정보를 가져올 수 없습니다. 인증이 필요합니다."));
        }

        UserInfoDto userInfoDto = UserInfoDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .kakaoId(user.getKakaoId())
                .loginType(user.getLoginType())
                .createdAt(user.getCreatedAt())
                .build();

        return ResponseEntity.ok(ApiResponse.success(userInfoDto, "정보 불러오기 성공!!"));
    }

}
