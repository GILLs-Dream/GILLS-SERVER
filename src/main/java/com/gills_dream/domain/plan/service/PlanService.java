package com.gills_dream.domain.plan.service;

import com.gills_dream.domain.map.dto.MapRouteByDayRes;
import com.gills_dream.domain.plan.dto.PlanListResponseDto;
import com.gills_dream.domain.plan.dto.PlanRequestDto;
import com.gills_dream.domain.plan.dto.PlanResponseDto;
import com.gills_dream.global.jwt.CustomUserDetails;

public interface PlanService {
    PlanResponseDto.MoodResultDto selectAndSummarizeMood(CustomUserDetails userDetails, PlanRequestDto.MoodTextDto request);
    PlanResponseDto.TemplateResultDto saveDuration(CustomUserDetails userDetails, Long planId, PlanRequestDto.DurationDto request);
    PlanResponseDto.TemplateResultDto saveCompanion(CustomUserDetails userDetails, Long planId, PlanRequestDto.CompanionDto request);
    PlanResponseDto.TemplateResultDto saveDestination(CustomUserDetails userDetails, Long planId, PlanRequestDto.DestinationDto request);
    PlanResponseDto.TemplateResultDto savStyle(CustomUserDetails userDetails, Long planId, PlanRequestDto.StyleDto request);
    PlanResponseDto.VideoResultDto saveVideo(CustomUserDetails userDetails, Long planId, PlanRequestDto.VideoUrlDto request);
    MapRouteByDayRes generate(CustomUserDetails userDetails, Long planId);
    PlanResponseDto.GenerateResultDto viewPlan(CustomUserDetails userDetails, Long planId);
    PlanResponseDto.SummaryDto viewPlanSummary(CustomUserDetails userDetails, Long planId);
    PlanListResponseDto getTemplateList();
    PlanResponseDto.TemplateResultDto setTitleAndImg(CustomUserDetails userDetails, Long planId);
}
