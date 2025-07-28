package com.example.matching_fit.domain.user.dto;

import com.example.matching_fit.domain.user.enums.LoginType;
import com.example.matching_fit.domain.user.enums.Role;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
@Getter
@Builder
public class UserInfoDto {
    private Long id;
    private String email;
    private String name;
    private String kakaoId;
    private LoginType loginType;
    private LocalDateTime createdAt;
    private String role;

}
