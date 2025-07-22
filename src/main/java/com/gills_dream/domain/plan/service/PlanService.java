package com.gills_dream.domain.plan.service;

import com.gills_dream.domain.plan.dto.PlanRequestDto;
import com.gills_dream.domain.plan.dto.PlanResponseDto;
import com.gills_dream.global.jwt.CustomUserDetails;

public interface PlanService {
    PlanResponseDto.MoodResultDto selectMood(CustomUserDetails userDetails, PlanRequestDto.MoodTextDto request);
}
