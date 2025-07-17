package com.example.matching_fit.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CheckEmailResponseDto {
    private boolean success;
    private boolean isDuplicated;
    private String message;
}
