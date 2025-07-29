package com.gills_dream.domain.plan.dto;

import com.gills_dream.domain.plan.enumerate.MoodType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public class PlanResponseDto {
    public record MoodResultDto (
            @Schema(description = "생성된 여행 id", example = "1")
            Long planId,
            @Schema(description = "사용자 요청 요약", example = "뿡삥뽕")
            String moodSummary,
            @Schema(description = "선택된 무드 타입 리스트", example = "[ HEALING, NATURE, RELAXED ]")
            List<MoodType> moodTypeList
    ) {}
}
