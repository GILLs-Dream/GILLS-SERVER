package com.gills_dream.domain.plan.service;

import com.gills_dream.domain.plan.dto.PlanRequestDto;
import com.gills_dream.domain.plan.dto.PlanResponseDto;
import com.gills_dream.global.jwt.CustomUserDetails;

public interface PlanService {
    PlanResponseDto.MoodResultDto selectAndSummarizeMood(CustomUserDetails userDetails, PlanRequestDto.MoodTextDto request);
    PlanResponseDto.TemplateResultDto saveDurationAndCompanion(CustomUserDetails userDetails, Long planId, PlanRequestDto.DurationAndCompanionDto request);
    PlanResponseDto.TemplateResultDto saveDestination(CustomUserDetails userDetails, Long planId, PlanRequestDto.DestinationDto request);
    PlanResponseDto.TemplateResultDto savStyle(CustomUserDetails userDetails, Long planId, PlanRequestDto.StyleDto request);
    PlanResponseDto.YoutubeResultDto saveYoutube(CustomUserDetails userDetails, Long planId, PlanRequestDto.YoutubeUrlDto request);
}
