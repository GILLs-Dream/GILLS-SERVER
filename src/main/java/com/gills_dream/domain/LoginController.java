package com.gills_dream.domain;

import com.gills_dream.domain.member.service.MemberService;
import com.gills_dream.global.apiPayload.ApiResponse;
import com.gills_dream.global.common.record.TokenPair;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "로그인")
@RestController
@RequiredArgsConstructor
@RequestMapping("/login")
public class LoginController {
    private final MemberService memberService;

    // 카카오가 code를 실어 리다이렉트하는 엔드포인트 (GET)
    @GetMapping("/link/kakao")
    public ApiResponse<TokenPairRes> kakaoCallback(
            @RequestParam String code,
            @RequestParam(required = false) String state
    ) {
        return ApiResponse.onSuccess(memberService.kakaoLoginTest(code));
    }
}