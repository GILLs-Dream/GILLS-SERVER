package com.gills_dream.domain.gpt.dto;

public class GptResponseDto {
    public record PlaceJson (
            String name,
            String type,
            String startedAt,
            String finishedAt,
            String duration
    ) {}
}
