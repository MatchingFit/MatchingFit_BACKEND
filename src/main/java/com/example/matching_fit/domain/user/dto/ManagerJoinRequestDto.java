package com.example.matching_fit.domain.user.dto;

import com.example.matching_fit.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ManagerJoinRequestDto {
    private String email;
    private String password;
    private String passwordConfirm;
    private String name;
    private String companyName;

    public static User from(ManagerJoinRequestDto joinUserDto){

        return User.builder()
                .email(joinUserDto.getEmail())
                .password(joinUserDto.getPassword())
                .name(joinUserDto.getName())
                .companyName(joinUserDto.getCompanyName())
                .build();
    }

}
