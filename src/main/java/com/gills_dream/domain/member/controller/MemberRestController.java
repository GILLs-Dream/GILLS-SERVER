package com.gills_dream.domain.member.controller;

import com.gills_dream.domain.member.converter.MemberConverter;
import com.gills_dream.domain.member.dto.MemberRequestDto;
import com.gills_dream.domain.member.dto.MemberResponseDto;
import com.gills_dream.global.apiPayload.code.status.SuccessStatus;
import com.gills_dream.domain.member.service.MemberService;
import com.gills_dream.global.apiPayload.ApiResponse;
import com.gills_dream.global.common.record.TokenPair;
import com.gills_dream.global.jwt.CustomUserDetails;
import com.gills_dream.global.jwt.JwtAuthenticationFilter;
import com.gills_dream.global.jwt.TokenUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "멤버", description = "멤버 로그인 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/member")
public class MemberRestController {
    private final MemberService memberService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final TokenUtils tokenUtils;

    @PostMapping("/oauth/kakao/login")
    @Operation(summary = "카카오 로그인, 회원가입", description = "엑세스 토큰 받아서 JWT 토큰 반환")
    public ResponseEntity<?> kakaoLogin(@RequestParam("accessToken") String accessToken) {
        TokenPair tokenPair = memberService.kakaoLoginWithToken(accessToken);

        // Token 처리
        HttpHeaders headers = new HttpHeaders();
        tokenUtils.addToken(headers, tokenPair, true);

        return ResponseEntity.ok().headers(headers).body(MemberConverter.toLoginResultWithTokenDto(tokenPair));
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "레디스에서 관련 키를 블랙리스트로 처리합니다.")
    public ApiResponse<MemberResponseDto.LoginResultDto> logout(HttpServletRequest request, @AuthenticationPrincipal CustomUserDetails userDetails) {
        memberService.logout(jwtAuthenticationFilter.resolveToken(request), userDetails.getMember());
        return ApiResponse.onSuccess(SuccessStatus._OK);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "탈퇴", description = "레디스에서 관련 키를 블랙리스트로 처리합니다.")
    public ApiResponse<MemberResponseDto.LoginResultDto> delete(HttpServletRequest request, @AuthenticationPrincipal CustomUserDetails userDetails) {
        memberService.delete(jwtAuthenticationFilter.resolveToken(request), userDetails.getMember());
        return ApiResponse.onSuccess(SuccessStatus._OK);
    }

    @PatchMapping("/setting")
    @Operation(summary = "프로필 설정 + 이용약관 동의")
    public ApiResponse<MemberResponseDto.ProfileResultDto> setProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                      @RequestBody @Valid MemberRequestDto.ProfileDto request) {
        return ApiResponse.onSuccess(memberService.setProfile(userDetails.getMember(), request));
    }
}

