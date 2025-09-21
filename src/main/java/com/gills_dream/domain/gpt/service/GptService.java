package com.gills_dream.domain.gpt.service;

import com.gills_dream.domain.map.dto.MapRouteByDayRes;
import com.gills_dream.domain.map.dto.MapRouteReq;
import com.gills_dream.domain.place.record.PlacePair;
import com.gills_dream.domain.plan.dto.PlanResponseDto;
import com.gills_dream.domain.plan.entity.Plan;
import com.gills_dream.domain.plan.enumerate.MoodType;
import com.gills_dream.domain.python.dto.PythonResponseDto;

import java.util.List;

public interface GptService {
    List<MoodType> selectMood(String input);
    String summarizeInput(String input);
    String summarizePlan(Plan plan);
    PlacePair filterPlace(PythonResponseDto.ProcessResultDto processDto, String region);
    Boolean checkRegion(String region);
    MapRouteByDayRes createRouteRequest(MapRouteReq routeReq, String transportType, Long count, List<String> categories, String moodSummary);
}
