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
import org.springframework.web.bind.annotation.*;

@Tag(name = "여행 생성", description = "여행 생성 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/plan/template")
public class PlanRestController {
    private final PlanService planService;

    @PostMapping("/mood")
    @Operation(summary = "여행 분위기 ai로 저장, Plan 생성", description = "MoodType 선택, 문장 요약")
    public ApiResponse<PlanResponseDto.MoodResultDto> selectMood (@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                  @Valid @RequestBody PlanRequestDto.MoodTextDto request) {
        return ApiResponse.onSuccess(planService.selectAndSummarizeMood(userDetails, request));
    }

    @PatchMapping("/{planId}/duration")
    @Operation(summary = "'얼마나?'에 해당하는 부분 저장")
    public ApiResponse<PlanResponseDto.TemplateResultDto> saveDuration (@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                        @PathVariable("planId") Long planId,
                                                                        @Valid @RequestBody PlanRequestDto.DurationDto request) {
        return ApiResponse.onSuccess(planService.saveDuration(userDetails, planId, request));
    }

    @PatchMapping("{planId}/companion")
    @Operation(summary = "'누구와?'에 해당하는 부분 저장")
    public ApiResponse<PlanResponseDto.TemplateResultDto> saveCompanion (@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                         @PathVariable("planId") Long planId,
                                                                         @Valid @RequestBody PlanRequestDto.CompanionDto request) {
        return ApiResponse.onSuccess(planService.saveCompanion(userDetails, planId, request));
    }

    @PatchMapping("/{planId}/style")
    @Operation(summary = "'어떻게?'에 해당하는 부분 저장")
    public ApiResponse<PlanResponseDto.TemplateResultDto> saveStyle (@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                     @PathVariable("planId") Long planId,
                                                                  @Valid @RequestBody PlanRequestDto.StyleDto request) {
        return ApiResponse.onSuccess(planService.savStyle(userDetails, planId, request));
    }

    @PostMapping("/{planId}/destination")
    @Operation(summary = "'어디로?'에 해당하는 부분 저장")
    public ApiResponse<PlanResponseDto.TemplateResultDto> saveDestination (@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                           @PathVariable("planId") Long planId,
                                                                           @Valid @RequestBody PlanRequestDto.DestinationDto request) {
        return ApiResponse.onSuccess(planService.saveDestination(userDetails, planId, request));
    }

    @PatchMapping("/{planId}/videos")
    @Operation(summary = "여행 장소 추천 위한 유튜브 링크 받음 (지역 선택 포함)")
    public ApiResponse<PlanResponseDto.VideoResultDto> saveVideo (@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                      @PathVariable("planId") Long planId,
                                                                      @Valid @RequestBody PlanRequestDto.VideoUrlDto request) {
        return ApiResponse.onSuccess(planService.saveVideo(userDetails, planId, request));
    }

//    @PostMapping("/{planId}/generate")
//    @Operation(summary = "템플릿 정보 기반 여행 생성")
//    public ApiResponse<PlanResponseDto.GenerateResultDto> generate (@AuthenticationPrincipal CustomUserDetails userDetails,
//                                                                    @PathVariable("planId") Long planId) {
//        return ApiResponse.onSuccess(planService.generate(userDetails, planId));
//    }
}
