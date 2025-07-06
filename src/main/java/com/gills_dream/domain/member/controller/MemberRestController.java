package com.gills_dream.domain.member.controller;

import com.gills_dream.domain.member.converter.MemberConverter;
import com.gills_dream.domain.member.dto.MemberResponseDto;
import com.gills_dream.global.common.TokenPair;
import com.gills_dream.domain.member.service.MemberService;
import com.gills_dream.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.Duration;

@Tag(name = "멤버", description = "멤버 로그인 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberRestController {
    private final MemberService memberService;

    @GetMapping("/oauth/kakao/login")
    @Operation(summary = "카카오 로그인, 회원가입", description = "프론트가 로그인 api를 호출하면서 백엔드에게 인가 코드를 넘겨줍니다")
    public ResponseEntity<ApiResponse<MemberResponseDto.LoginResultDto>> kakaoLogin(@RequestParam("code") String code) throws IOException {
        TokenPair tokenPair= memberService.kakaoLogin(code);

        // 1. Authorization 헤더에 Access Token 추가
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + tokenPair.accessToken());

        // 2. Refresh Token은 HttpOnly, Secure 쿠키로 설정
        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", tokenPair.refreshToken())
                .httpOnly(true)
                .secure(false) // 배포할때 true로 변경
                .path("/")
                .sameSite("Strict")
                .maxAge(Duration.ofDays(28)) // 4주
                .build();
        headers.add(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        // 3. Response Body 처리
        ApiResponse<MemberResponseDto.LoginResultDto> body = ApiResponse.onSuccess(MemberConverter.toLoginResultDto(tokenPair.member()));

        return ResponseEntity.ok().headers(headers).body(body);
    }
}

