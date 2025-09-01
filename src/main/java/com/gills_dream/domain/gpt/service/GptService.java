package com.gills_dream.domain.gpt.service;

import com.gills_dream.domain.map.dto.MapRouteReq;
import com.gills_dream.domain.plan.enumerate.MoodType;
import com.gills_dream.domain.python.dto.PythonResponseDto;

import java.util.List;

public interface GptService {
    List<MoodType> selectMood(String input);
    String summarizeInput(String input);
    List<String> filterPlace(PythonResponseDto.ProcessResultDto processDto, String region);
    MapRouteReq createRouteRequest(MapRouteReq routeReq, String transportType, Long count);
}
