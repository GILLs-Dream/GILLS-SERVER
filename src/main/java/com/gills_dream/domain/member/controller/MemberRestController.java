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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "멤버", description = "멤버 로그인 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/member")
public class MemberRestController {
    private final MemberService memberService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final TokenUtils tokenUtils;

    @PostMapping("/oauth/kakao/login")
    @Operation(summary = "카카오 로그인, 회원가입", description = "access token 받아서 서버 JWT 토큰 반환")
    public ResponseEntity<?> kakaoLogin(@RequestParam("accessToken") String accessToken) {
        MemberResponseDto.TokenPairWithFlagDto tokenPairWithFlagDto = memberService.kakaoLogin(accessToken);

        // Token 처리
        HttpHeaders headers = new HttpHeaders();
        tokenUtils.addToken(headers, tokenPairWithFlagDto.tokenPair(), true);

        return ResponseEntity.ok().headers(headers).body(MemberConverter.toLoginResultWithTokenDto(tokenPairWithFlagDto));
    }

    @PostMapping("/oauth/apple/login")
    @Operation(summary = "애플 로그인, 회원가입", description = "identity token 받아서 서버 JWT 토큰 반환")
    public ResponseEntity<?> appleLogin(@RequestParam("identityToken") String identityToken) {
        MemberResponseDto.TokenPairWithFlagDto tokenPairWithFlagDto = memberService.appleLogin(identityToken);

        // Token 처리
        HttpHeaders headers = new HttpHeaders();
        tokenUtils.addToken(headers, tokenPairWithFlagDto.tokenPair(), true);

        return ResponseEntity.ok().headers(headers).body(MemberConverter.toLoginResultWithTokenDto(tokenPairWithFlagDto));
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "레디스에서 관련 키를 블랙리스트로 처리합니다.")
    public ApiResponse<?> logout(HttpServletRequest request, @AuthenticationPrincipal CustomUserDetails userDetails) {
        memberService.logout(jwtAuthenticationFilter.resolveToken(request), userDetails.getMember());
        return ApiResponse.onSuccess(SuccessStatus._OK);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "탈퇴", description = "레디스에서 관련 키를 블랙리스트로 처리합니다.")
    public ApiResponse<?> delete(HttpServletRequest request, @AuthenticationPrincipal CustomUserDetails userDetails) {
        memberService.delete(jwtAuthenticationFilter.resolveToken(request), userDetails.getMember());
        return ApiResponse.onSuccess(SuccessStatus._OK);
    }

    @PatchMapping(value = "/setting", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "프로필 설정 + 이용약관 동의 + 프로필 이미지 설정(이미지 이름에 확장자 꼭 추가해주세요)")
    public ApiResponse<MemberResponseDto.ProfileResultDto> setProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                      @RequestPart("profile") @Valid MemberRequestDto.ProfileDto request,
                                                                      @RequestPart(value = "image", required = false) MultipartFile image) {
        return ApiResponse.onSuccess(memberService.setProfile(userDetails, request, image));
    }

    @GetMapping("/info")
    @Operation(summary = "본인 닉네임, 이메일 조회")
    public ApiResponse<MemberResponseDto.MemberInfoDto> info(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.onSuccess(memberService.info(userDetails));
    }

    @PatchMapping(value = "/setting/edit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "프로필 이미지, 이용약관 동의, 닉네임 수정")
    public ApiResponse<MemberResponseDto.ProfileResultDto> editProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                      @RequestPart("profile") @Valid MemberRequestDto.ProfileDto request,
                                                                      @RequestPart(value = "image", required = false) MultipartFile image) {
        return ApiResponse.onSuccess(memberService.editProfile(userDetails, request, image));
    }

    @PatchMapping(value = "/setting/edit-email")
    @Operation(summary = "이메일 수정")
    public ResponseEntity<?> editEmail(@AuthenticationPrincipal CustomUserDetails userDetails,
                                    @Valid @RequestBody MemberRequestDto.EmailDto request) {
        MemberResponseDto.EmailWithTokenDto emailWithTokenDto = memberService.editEmail(userDetails, request);

        // Token 처리
        HttpHeaders headers = new HttpHeaders();
        tokenUtils.addToken(headers, emailWithTokenDto.tokenPair(), true);

        return ResponseEntity.ok().headers(headers).body(MemberConverter.toEditEmailResultWithTokenDto(emailWithTokenDto.tokenPair()));
    }
}

