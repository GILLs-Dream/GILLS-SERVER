package com.gills_dream.domain.plan.dto;

import com.gills_dream.domain.plan.enumerate.CategoryType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

public class PlanRequestDto {
    public record MoodTextDto (
            @Schema(description = "분위기 관련 설명", example = "가을에 찐친들과 여유롭고 유쾌한 분위기의 여행을 제주도에서 즐기고 싶어")
            String inputText
    ) {}

    public record DurationAndCompanionDto(
            @Schema(description = "여행 기간 (최대 7일)", example = "5")
            Long duration,
            @Schema(description = "여행 시작 날짜 (nullable)", example = "2025-07-25")
            LocalDate startDate,
            @Schema(description = "여행 끝 날짜 (nullable)", example = "2025-07-25")
            LocalDate finishedDate,
            @Schema(description = "인원수", example = "5")
            Long party,
            @Schema(description = "누구와", example = "길쑤니들")
            String companion
    ) {}

    public record DestinationDto (
            @Schema(description = "여행 장소 리스트")
            List<TravelPlaceDto> travelPlaceDtoList,
            @Schema(description = "숙박 장소 리스트")
            List<StayPlaceDto> stayPlaceDtoList
    ) {}

    public record TravelPlaceDto (
            @Schema(description = "여행 장소 이름", example = "동궁과 월지")
            String placeName,
            @Schema(description = "방문 날짜 (nullable)", example = "2025-07-25")
            String date,
            @Schema(description = "여행 장소 썸네일 이미지", example = "https://...")
            String img
    ) {}

    public record StayPlaceDto (
            @Schema(description = "숙박 장소 이름", example = "길쑨스테이")
            String placeName,
            @Schema(description = "숙박 장소 썸네일 이미지", example = "https://...")
            String img,
            @Schema(description = "숙박 시작 날짜 (nullable)", example = "2025-07-25")
            LocalDate startDate,
            @Schema(description = "숙박 끝 날짜 (nullable)", example = "2025-07-25")
            LocalDate endDate
    ) {}

    public record StyleDto (
            @Schema(description = "주요 교통수단 [ 도보, 대중교통, 자동차, 자전거 ]", example = "도보")
            String transport,
            @Schema(description = "선호 카테고리 리스트", example = "[ \"음식\", \"자연\", \"문화\"]")
            List<String> categoryTypeList
    ) {}

    public record YoutubeUrlDto (
            @Schema(description = "유튜브 링크 리스트")
            List<String> youtubeUrlList
    ) {}
}
