package com.gills_dream.domain.member.controller;

import com.gills_dream.domain.member.converter.MemberConverter;
import com.gills_dream.domain.member.dto.MemberResponseDto;
import com.gills_dream.global.apiPayload.code.status.SuccessStatus;
import com.gills_dream.global.common.record.TokenPair;
import com.gills_dream.domain.member.service.MemberService;
import com.gills_dream.global.apiPayload.ApiResponse;
import com.gills_dream.global.jwt.CustomUserDetails;
import com.gills_dream.global.jwt.JwtAuthenticationFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @PostMapping("/oauth/kakao/login")
    @Operation(summary = "카카오 로그인, 회원가입", description = "프론트가 로그인 api를 호출하면서 백엔드에게 인가 코드를 넘겨줍니다")
    public ResponseEntity<ApiResponse<MemberResponseDto.LoginResultDto>> kakaoLogin(@RequestParam("code") String code) throws IOException {
        TokenPair tokenPair= memberService.kakaoLogin(code);

        // Authorization 헤더에 access token 추가
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + tokenPair.accessToken());

        // Refresh token은 HttpOnly, secure 쿠키로 설정
        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", tokenPair.refreshToken())
                .httpOnly(true)
                .secure(false) // 배포할때 true로 변경
                .path("/")
                .sameSite("Strict")
                .maxAge(Duration.ofDays(28)) // 4주
                .build();
        headers.add(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        // Response body 처리
        ApiResponse<MemberResponseDto.LoginResultDto> body = ApiResponse.onSuccess(MemberConverter.toLoginResultDto(tokenPair.member()));

        return ResponseEntity.ok().headers(headers).body(body);
    }

    @PostMapping("/oauth/kakao/logout")
    @Operation(summary = "카카오 로그아웃")
    public ApiResponse<MemberResponseDto.LoginResultDto> kakaoLogout(HttpServletRequest request, @AuthenticationPrincipal CustomUserDetails userDetails) {
        memberService.kakaoLogout(jwtAuthenticationFilter.resolveToken(request), userDetails.getMember());
        return ApiResponse.onSuccess(SuccessStatus._OK);
    }

    @GetMapping("/oauth/reissue")
    @Operation(summary = "리프레시 토큰으로 엑세스 토큰 재발급", description = "프론트가 쿠키로 요청합니다")
    public ResponseEntity<?> reissue(@CookieValue("refresh_token") String refreshToken) {
        TokenPair tokenPair = memberService.reissue(refreshToken);

        // Authorization 헤더에 새로운 access token 추가
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + tokenPair.accessToken());

        // 만약 refresh token이 재발급 되었다면 HttpOnly, secure 쿠키로 설정
        if (!refreshToken.equals(tokenPair.refreshToken())) {
            ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", tokenPair.refreshToken())
                    .httpOnly(true)
                    .secure(false) // 배포할때 true로 변경
                    .path("/")
                    .sameSite("Strict")
                    .maxAge(Duration.ofDays(28)) // 4주
                    .build();
            headers.add(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        }

        return ResponseEntity.ok().headers(headers).body("Access token reissued");
    }

}

