package com.gills_dream.global.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class TokenUtils {
    public void addAccessToken(HttpHeaders headers, String accessToken) {
        // Authorization 헤더에 access token 추가
        headers.add("Authorization", "Bearer " + accessToken);
    }

    public void addRefreshToken(HttpHeaders headers, String refreshToken) {
        // Refresh token은 HttpOnly, secure 쿠키로 설정
        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(false) // 배포할때 true로 변경
                .path("/")
                .sameSite("Strict")
                .maxAge(Duration.ofDays(28)) // 4주
                .build();
        headers.add(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }
}
