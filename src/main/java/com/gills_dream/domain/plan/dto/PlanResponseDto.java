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

    public record TemplateResultDto (
            @Schema(description = "여행 id", example = "1")
            Long planId
    ) {}

    public record VideoResultDto (
            @Schema(description = "여행 id", example = "1")
            Long planId,
            @Schema(description = "추천 여행 장소 리스트")
            List<String> recommendedList
    ) {}

    public record PlanDraftDto (
            @Schema(description = "여행 총 일수", example = "3")
            Long duration,
            @Schema(description = "하루 추천 일정")
            List<DayDraftDto> draftDtoList
    ) {}

    public record DayDraftDto (
            @Schema(description = "숙박 리스트")
            List<PlaceDraftDto> accommodationList,
            @Schema(description = "여행지 리스트")
            List<PlaceDraftDto> travelDestList,
            @Schema(description = "식당 리스트")
            List<PlaceDraftDto> restaurantList,
            @Schema(description = "카페 리스트")
            List<PlaceDraftDto> cafeList
    ) {}

    public record PlaceDraftDto (
            @Schema(description = "장소 이름")
            String name,
            @Schema(description = "방문일")
            String visitAt
    ) {}

    public record GenerateResultDto (
            @Schema(description = "여행 id", example = "1")
            Long planId,
            @Schema(description = "여행 총 일수", example = "3")
            Long duration,
            @Schema(description = "하루별 여행지 추천 리스트")
            List<PlanPerDayDto> planPerDayDtoList,
            @Schema(description = "여행 전체 요약", example = "이 여행은 아주 흥미롭군요...")
            String summary
    ) {}

    public record PlanPerDayDto (
            @Schema(description = "n일차", example = "1")
            Long dayNum,
            @Schema(description = "시작 장소")
            PlaceDto from,
            @Schema(description = "장소와 거리 정보 리스트")
            List<RouteDto> routeDtoList
    ) {}

    public record RouteDto (
            @Schema(description = "순서", example = "1")
            Long cnt,
            @Schema(description = "to 장소 정보")
            PlaceDto to,
            @Schema(description = "거리 km", example = "3.1")
            Double distance,
            @Schema(description = "소요 시간 min", example = "30")
            Long time
    ) {}

    public record PlaceDto (
            @Schema(description = "장소 id", example = "1")
            Long placeId,
            @Schema(description = "장소 이름", example = "동궁과월지")
            String name,
            @Schema(description = "장소 대표 이미지", example = "https://...")
            String mainImg
    ) {}
}
