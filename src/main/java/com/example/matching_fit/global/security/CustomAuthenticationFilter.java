package com.example.matching_fit.global.security;

import com.example.matching_fit.domain.user.entity.User;
import com.example.matching_fit.domain.user.service.RedisService;
import com.example.matching_fit.domain.user.service.UserService;
import com.example.matching_fit.global.security.rq.Rq;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthenticationFilter extends OncePerRequestFilter {
    private final UserService userService;
    private final Rq rq;
    private final RedisService redisService;

    record AuthTokens(String refreshToken, String accessToken) {
    }

    // ✅ Swagger 관련 경로 예외 처리
    private boolean isSwaggerRequest(String uri) {
        return uri.startsWith("/swagger-ui")
                || uri.startsWith("/v3/api-docs")
                || uri.startsWith("/swagger-resources")
                || uri.startsWith("/webjars")
                || uri.startsWith("/v2/api-docs")
                || uri.contains("swagger")
                || uri.equals("/swagger-ui.html");
    }

    private AuthTokens getAuthTokensFromRequest() {
        String authorization = rq.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String accessToken = authorization.substring("Bearer ".length()).trim();
            String refreshToken = rq.getCookieValue("refreshToken");
            return new AuthTokens(refreshToken, accessToken);
        }

        String refreshToken = rq.getCookieValue("refreshToken");
        String accessToken = rq.getCookieValue("accessToken");

        if (refreshToken != null && accessToken != null)
            return new AuthTokens(refreshToken, accessToken);

        return null;
    }


    private void refreshAccessToken(User user) {
        String newAccessToken = userService.genAccessToken(user);
        rq.setHeader("Authorization", "Bearer " + user.getRefreshToken());
        rq.setCookie("accessToken", newAccessToken);
    }

    private User refreshAccessTokenByRefreshToken(String refreshToken) {
        Optional<User> opMemberByRefreshToken = userService.findByRefreshToken(refreshToken);

        if (opMemberByRefreshToken.isEmpty()) {
            return null;
        }

        User user = opMemberByRefreshToken.get();

        refreshAccessToken(user);

        return user;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        if (!requestURI.startsWith("/api/") || isSwaggerRequest(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        AuthTokens authTokens = getAuthTokensFromRequest();
        if (authTokens == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String refreshToken = authTokens.refreshToken;
        String accessToken = authTokens.accessToken;

        User user = null;

        // ✅ 1) accessToken 유효성 체크 + Redis 유효성 체크
        if (redisService.isAccessTokenValid(accessToken)) {
            user = userService.getUserFromAccessToken(accessToken);
        }

        // ✅ 2) accessToken이 없거나 무효 → refreshToken 재발급 시도
        if (user == null && refreshToken != null) {
            user = refreshAccessTokenByRefreshToken(refreshToken);
        }

        // ✅ 3) 로그인 상태 설정
        if (user != null) {
            rq.setLogin(user);
        }

        filterChain.doFilter(request, response);
    }
}
