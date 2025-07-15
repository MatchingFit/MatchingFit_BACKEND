package com.example.matching_fit.domain.user.controller;

import com.example.matching_fit.domain.user.dto.CheckEmailResponseDto;
import com.example.matching_fit.domain.user.dto.LoginRequestDto;
import com.example.matching_fit.domain.user.dto.LoginResponseDto;
import com.example.matching_fit.domain.user.dto.UserJoinRequestDto;
import com.example.matching_fit.domain.user.entity.User;
import com.example.matching_fit.domain.user.enums.LoginType;
import com.example.matching_fit.domain.user.service.UserService;
import com.example.matching_fit.global.rp.ApiResponse;
import com.example.matching_fit.global.security.rq.Rq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "user 관련 API")
@Slf4j
public class UserController {

    private final UserService userService;
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
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(@RequestBody LoginRequestDto user) {
        try {
            String token = userService.login(user.getEmail(), user.getPassword());
            User loginUser = userService.findByEmail(user.getEmail()).orElseThrow();
            LoginResponseDto loginResponseDto = new LoginResponseDto(token, loginUser.getName());
            return ResponseEntity.ok(ApiResponse.success(loginResponseDto, "login success"));
        } catch (BadCredentialsException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail(e.getMessage()));
        }
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

}
