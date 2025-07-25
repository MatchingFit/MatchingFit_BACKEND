package com.example.matching_fit.global.config.security;

import com.example.matching_fit.global.security.CustomAuthenticationEntryPoint;
import com.example.matching_fit.global.security.CustomAuthenticationFilter;
import com.example.matching_fit.global.security.oauth2.CustomAuthorizationRequestResolver;
import com.example.matching_fit.global.security.oauth2.CustomOAuth2AuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true) // @PreAuthorize, @PostAuthorize 활성화
public class SecurityConfig {
    private final CustomAuthenticationFilter customAuthenticationFilter;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomOAuth2AuthenticationSuccessHandler customOAuth2AuthenticationSuccessHandler;
    private final CustomAuthorizationRequestResolver customAuthorizationRequestResolver;

    //통과 시킬꺼 넣어야함

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .addFilterBefore(customAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests( auth -> auth

                        // GET 요청 허용
                        .requestMatchers(HttpMethod.GET, PermitUrl.GET_URLS).permitAll()
                        // POST 요청 허용
                        .requestMatchers(HttpMethod.POST, PermitUrl.POST_URLS).permitAll()
                        // PUT 요청 허용
                        .requestMatchers(HttpMethod.PUT, PermitUrl.PUT_URLS).permitAll()
                        // PATCH 요청 허용
                        .requestMatchers(HttpMethod.PATCH, PermitUrl.PATCH_URLS).permitAll()
                        // DELETE 요청 허용
                        .requestMatchers(HttpMethod.DELETE, PermitUrl.DELETE_URLS).permitAll()
                        // 모든 요청 허용 (ALL_URLS)
                        .requestMatchers(PermitUrl.ALL_URLS).permitAll()
                        // 나머지 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .formLogin( form -> form.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .formLogin(
                        AbstractHttpConfigurer::disable
                )
                .sessionManagement((sessionManagement) -> sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .oauth2Login(
                        oauth2Login ->
                        oauth2Login
                                .successHandler(customOAuth2AuthenticationSuccessHandler)
                                .authorizationEndpoint(
                                        authorizationEndpoint ->
                                                authorizationEndpoint
                                                        .authorizationRequestResolver(customAuthorizationRequestResolver)
                                )
                )
                .exceptionHandling(ex ->
                        ex.authenticationEntryPoint(customAuthenticationEntryPoint)
                );


        ; //h2-console 접근 허용
        return http.build();
    }
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost","http://localhost:5173", "https://localhost:8080", "http://localhost:8000")); // 요청 도메인
        config.setAllowedMethods(List.of("GET", "POST", "OPTIONS", "PUT", "DELETE", "PATCH"));

        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        // SSE를 위한 추가 헤더 설정
        config.setExposedHeaders(List.of("Last-Event-ID", "Cache-Control", "Connection"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
    @Bean
    public UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager(); // 빈 등록만 하고 사용자 추가 X
    }


}
