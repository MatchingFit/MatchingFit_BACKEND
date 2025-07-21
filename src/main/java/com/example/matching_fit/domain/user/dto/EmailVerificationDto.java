package com.example.matching_fit.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class EmailVerificationDto {
    @NotBlank(message = "email은 필수입니다.")
    String email;
    @NotBlank(message = "code는 필수입니다.")
    String code;
}
