package com.example.matching_fit.global.config.security;

public class PermitUrl {

    //모든 메서드 요청 허용
    public static final String[] ALL_URLS = {
            "http://localhost:8080/oauth2/authorization/kakao",
            "http://localhost/api/oauth2/authorization/kakao",
    };
    public static final String[] GET_URLS = {
            "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html","/api/v1/users/check-email",
            "/api/v1/managers/**","/api/v1/competencyscores/**"

    };

    public static final String[] POST_URLS = {
            "/api/v1/users/**", "/api/v1/email/verify",
            "/api/v1/email/join/send", "/api/v1/token/**","/api/v1/gpt/**","/api/v1/managers/**"
    };

    public static final String[] PUT_URLS = {
            // PUT 메서드에 해당하는 경로를 여기에 추가

    };

    public static final String[] DELETE_URLS = {
            // DELETE 메서드에 해당하는 경로를 여기에 추가

    };

    public static final String[] PATCH_URLS = {
            // 다른 PATCH 엔드포인트가 있으면 여기에 추가

    };



}
