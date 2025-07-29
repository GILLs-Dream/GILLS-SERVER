package com.gills_dream.domain.python.service;

import com.gills_dream.domain.plan.dto.PlanRequestDto;

import java.util.List;

public interface PythonService {
    List<String> process_youtube(PlanRequestDto.YoutubeUrlDto request);
}
