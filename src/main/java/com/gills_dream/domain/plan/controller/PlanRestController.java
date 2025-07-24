package com.gills_dream.domain.plan.controller;

import com.gills_dream.domain.plan.dto.PlanResponseDto;
import com.gills_dream.domain.plan.dto.PlanRequestDto;
import com.gills_dream.domain.plan.service.PlanService;
import com.gills_dream.global.apiPayload.ApiResponse;
import com.gills_dream.global.jwt.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "여행 생성", description = "여행 생성 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/plan/template")
public class PlanRestController {
    private final PlanService planService;

    @PostMapping("/mood")
    @Operation(summary = "여행 분위기 ai로 저장", description = "MoodType 셀렉트, 문장 요약")
    public ApiResponse<PlanResponseDto.MoodResultDto> selectMood (@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                  @Valid @RequestBody PlanRequestDto.MoodTextDto request) {
        return ApiResponse.onSuccess(planService.selectAndSummarizeMood(userDetails, request));
    }
}
