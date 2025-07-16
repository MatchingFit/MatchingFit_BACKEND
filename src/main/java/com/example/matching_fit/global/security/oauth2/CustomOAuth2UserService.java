package com.example.matching_fit.global.security.oauth2;

import com.example.matching_fit.domain.user.entity.User;
import com.example.matching_fit.domain.user.service.UserService;
import com.example.matching_fit.global.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    // 소셜 로그인이 성공할 때마다 이 함수가 실행된다.
    private final UserService userService;
    @Transactional
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String oauthId = oAuth2User.getName();
        String providerTypeCode = userRequest
                .getClientRegistration()
                .getRegistrationId()
                .toUpperCase(Locale.getDefault());
        Map<String, Object> attributes = oAuth2User.getAttributes();
        Map<String, String> attributesProperties = (Map<String, String>) attributes.get("properties");
        String nickname = attributesProperties.get("nickname");
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        String email = (String) kakaoAccount.get("email");
        String kakaoId = providerTypeCode + "__" + oauthId; //성공하면 이메일(email)로 변경 예정
        User user = userService.modifyOrJoin(email, nickname, kakaoId);
        return new SecurityUser(
                user.getId(),
                user.getEmail(),
                "",
                user.getName(),
                user.getAuthorities()
        );
    }

}