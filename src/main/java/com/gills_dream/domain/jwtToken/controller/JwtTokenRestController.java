package com.gills_dream.domain.jwtToken.controller;

import com.gills_dream.domain.jwtToken.service.JwtTokenService;
import com.gills_dream.global.common.record.TokenPair;
import com.gills_dream.global.jwt.TokenUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "토큰", description = "토큰 관련 API")
@RestController
@RequiredArgsConstructor
public class JwtTokenRestController {
    private final TokenUtils tokenUtils;
    private final JwtTokenService jwtTokenService;

    @GetMapping("member/reissue")
    @Operation(summary = "리프레시 토큰으로 엑세스 토큰 재발급", description = "프론트가 쿠키로 요청합니다")
    public ResponseEntity<?> reissue(@CookieValue("refresh_token") String refreshToken) {
        TokenPair tokenPair = jwtTokenService.reissue(refreshToken);

        // Token 처리
        HttpHeaders headers = new HttpHeaders();
        tokenUtils.addAccessToken(headers, tokenPair.accessToken());
        // Refresh token이 재발급된 경우
        if (!refreshToken.equals(tokenPair.refreshToken())) tokenUtils.addRefreshToken(headers, tokenPair.refreshToken());

        return ResponseEntity.ok().headers(headers).body("Access token reissued");
    }
}
