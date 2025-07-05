package com.gills_dream.domain.member.controller;

import com.gills_dream.domain.member.converter.MemberConverter;
import com.gills_dream.domain.member.dto.MemberResponseDto;
import com.gills_dream.domain.member.service.MemberService;
import com.gills_dream.global.apiPayload.ApiResponse;
import com.gills_dream.global.apiPayload.code.status.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "멤버", description = "멤버 로그인 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberRestController {
    @GetMapping("/oauth/kakao/login")
    @Operation(summary = "카카오 로그인")
    public ApiResponse<MemberConverter.LoginResultDto> kakaoLogin() {
        return null;
    }
}

