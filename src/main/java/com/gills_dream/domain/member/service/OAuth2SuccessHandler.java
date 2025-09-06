package com.gills_dream.domain.member.service;

import com.gills_dream.global.jwt.TokenProvider;
import com.gills_dream.global.redis.RedisService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final TokenProvider tokenProvider;
    private final RedisService redisService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = extractEmailFromPrincipal(oAuth2User);
        String accessToken = tokenProvider.createAccessToken(email);
        String refreshToken = tokenProvider.createRefreshToken(email);

        // Redis에 refreshToken 저장
        redisService.setValue(email, refreshToken, Duration.ofDays(28).toMillis());

        // 토큰을 프론트에 전달 (리디렉션 방식)
        String redirectUri = "http://localhost:3000/login/success?accessToken=" + accessToken + "&refreshToken=" + refreshToken;
        response.sendRedirect(redirectUri);
    }

    private String extractEmailFromPrincipal(OAuth2User user) {
        Map<String, Object> account = (Map<String, Object>) user.getAttributes().get("kakao_account");
        return (String) account.get("email");
    }
}
