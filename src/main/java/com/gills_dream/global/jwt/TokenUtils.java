package com.gills_dream.global.jwt;

import com.gills_dream.global.common.record.TokenPair;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class TokenUtils {
    public void addToken(HttpHeaders headers, TokenPair tokenPair, boolean reissued) {
        // Authorization 헤더에 access token 추가
        headers.add("Authorization", "Bearer " + tokenPair.accessToken());

        // refresh token이 재발급된 경우에만
        // X-Refresh-Token 헤더에 refresh token 추가
        if (reissued) {
            headers.add("X-Refresh-Token", tokenPair.refreshToken());
        }
    }
}
