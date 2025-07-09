package com.example.matching_fit.global.security.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomOAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {

//    private final SiteProperties siteProperties;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {

//        if (exception instanceof OAuth2AuthenticationProcessingException) {
//            // 신규 회원 추가 정보 입력 페이지로 리다이렉트
//            response.sendRedirect(siteProperties.getFrontUrl() +"/user/oauth2/additional-info");
//        } else {
//            // 일반 실패 페이지 등으로
//            response.sendRedirect(siteProperties.getFrontUrl() + "/user/login");
//        }
    }


}
