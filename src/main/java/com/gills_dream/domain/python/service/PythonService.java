package com.gills_dream.domain.python.service;

import com.gills_dream.domain.plan.dto.PlanRequestDto;
import com.gills_dream.domain.python.dto.PythonResponseDto;

public interface PythonService {
    PythonResponseDto.ProcessResultDto processVideo(PlanRequestDto.VideoUrlDto request);
}
