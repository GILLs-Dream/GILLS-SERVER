package com.gills_dream.domain.gpt.service;

import com.gills_dream.domain.place.record.PlacePair;
import com.gills_dream.domain.plan.dto.PlanResponseDto;
import com.gills_dream.domain.plan.entity.Plan;
import com.gills_dream.domain.plan.enumerate.MoodType;
import com.gills_dream.domain.python.dto.PythonResponseDto;

import java.util.List;

public interface GptService {
    List<MoodType> selectMood(String input);
    String summarizeInput(String input);
    PlacePair filterPlace(PythonResponseDto.ProcessResultDto processDto, String region);
    String summarizePlan(Long duration, List<PlanResponseDto.PlanPerDayDto> planPerDayList);
    PlanResponseDto.PlanDraftDto generatePlan(Plan plan);
}
