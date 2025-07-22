package com.gills_dream.domain.plan.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class PlanRequestDto {
    public record MoodTextDto (
            @Schema(description = "분위기 관련 설명", example = "가을에 찐친들과 chill하고 hot한 분위기의 여행을 제주도에서 즐기고 싶어")
            String inputText
    ) {}
}
