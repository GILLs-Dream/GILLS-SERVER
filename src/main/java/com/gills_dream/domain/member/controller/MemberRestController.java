package com.gills_dream.domain.member.controller;

import com.gills_dream.domain.member.dto.MemberRequestDto;
import com.gills_dream.domain.member.dto.MemberResponseDto;
import com.gills_dream.domain.token.service.CustomTokenService;
import com.gills_dream.global.apiPayload.code.status.SuccessStatus;
import com.gills_dream.global.common.record.ResponsePair;
import com.gills_dream.global.common.record.TokenPair;
import com.gills_dream.domain.member.service.MemberService;
import com.gills_dream.global.apiPayload.ApiResponse;
import com.gills_dream.global.jwt.CustomUserDetails;
import com.gills_dream.global.jwt.JwtAuthenticationFilter;
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
@RequestMapping("/member")
public class MemberRestController {
    private final MemberService memberService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomTokenService customTokenService;

    @PostMapping("/oauth/kakao/login")
    @Operation(summary = "카카오 로그인, 회원가입", description = "프론트가 로그인 api를 호출하면서 백엔드에게 인가 코드를 넘겨줍니다")
    public ResponseEntity<ApiResponse<MemberResponseDto.LoginResultDto>> kakaoLogin(@RequestParam("code") String code) {
        TokenPair tokenPair= memberService.kakaoLogin(code);
        ResponsePair responsePair = customTokenService.buildLoginResponse(tokenPair, new HttpHeaders());

        return ResponseEntity.ok().headers(responsePair.headers()).body(responsePair.body());
    }

    @PostMapping("/oauth/kakao/logout")
    @Operation(summary = "카카오 로그아웃")
    public ApiResponse<MemberResponseDto.LoginResultDto> kakaoLogout(HttpServletRequest request, @AuthenticationPrincipal CustomUserDetails userDetails) {
        memberService.kakaoLogout(jwtAuthenticationFilter.resolveToken(request), userDetails.getMember());
        return ApiResponse.onSuccess(SuccessStatus._OK);
    }

    @PatchMapping("/setting")
    @Operation(summary = "프로필 설정 + 이용약관 동의")
    public ApiResponse<MemberResponseDto.ProfileResultDto> setProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                      @RequestBody @Valid MemberRequestDto.ProfileDto request) {
        return ApiResponse.onSuccess(memberService.setProfile(userDetails.getMember(), request));
    }
}

