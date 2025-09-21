package com.gills_dream.domain.plan.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gills_dream.domain.place.enumerate.Platform;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PlanRequestDto {
    public record MoodTextDto (
            @Schema(description = "분위기 관련 설명", example = "가을에 찐친들과 여유롭고 유쾌한 분위기의 여행을 제주도에서 즐기고 싶어")
            String inputText
    ) {}

    public record DurationDto(
            @Schema(description = "여행 기간 (최대 7일)", example = "5")
            Integer duration,
            @Schema(description = "여행 시작 날짜 (nullable)", example = "2025-07-25")
            LocalDate startDate,
            @Schema(description = "여행 끝 날짜 (nullable)", example = "2025-07-25")
            LocalDate finishedDate
    ) {}

    public record CompanionDto (
            @Schema(description = "인원수", example = "5")
            Long party,
            @Schema(description = "누구와", example = "길쑤니들")
            String companion
    ) {}

    public record DestinationDto (
            @Schema(description = "여행 장소 리스트")
            List<TourPlaceDto> tourDtoList,
            @Schema(description = "숙박 장소 리스트")
            List<AccommodationDto> accommodationDtoList
    ) {}

    public record TourPlaceDto (
            @Schema(description = "플랫폼 타입 [TOUR, KAKAO, VIDEO]", example = "TOUR")
            Platform platform,
            @Schema(description = "content Id", example = "1234")
            String contentId,
            @Schema(description = "방문 날짜 (nullable)", example = "2025-07-25")
            String date,
            @Schema(description = "(날짜가 미정인 경우) 여행 몇일차에 갈지", example = "3")
            Integer dayOrder,
            @Schema(description = "장소명 (nullable)", example = "경복궁")
            String title,
            @Schema(description = "latitude (nullable)", example = "37.579617")
            String lat,
            @Schema(description = "longitude (nullable)", example = "126.977041")
            String lng,
            @Schema(description = "주소 (nullable)" , example = "서울특별시 종로구 사직로 161")
            String roadAddress,
            @Schema(description = "장소 상세정보 URL (nullable)", example = "http://place.map.kakao.com/18619553")
            String detailUrl
    ) {}

    public record AccommodationDto(
            @Schema(description = "플랫폼 타입 [TOUR, KAKAO, VIDEO]", example = "TOUR")
            Platform platform,
            @Schema(description = "content Id", example = "1234")
            String contentId,
            @Schema(description = "숙박 시작 날짜 (nullable)", example = "2025-07-25")
            String startDate,
            @Schema(description = "숙박 끝 날짜 (nullable)", example = "2025-07-25")
            String endDate,
            @Schema(description = "(날짜가 미정인 경우) 여행 몇일차에 갈지", example = "3")
            Integer dayOrder,
            @Schema(description = "장소명 (nullable)", example = "경복궁")
            String title,
            @Schema(description = "latitude (nullable)", example = "37.579617")
            String lat,
            @Schema(description = "longitude (nullable)", example = "126.977041")
            String lng,
            @Schema(description = "주소 (nullable)" , example = "서울특별시 종로구 사직로 161")
            String roadAddress,
            @Schema(description = "장소 상세정보 URL (nullable)", example = "http://place.map.kakao.com/18619553")
            String detailUrl
    ) {}

    public record StyleDto (
            @Schema(description = "주요 교통수단 [ 도보, 대중교통, 자동차, 자전거 ]", example = "도보")
            String transport,
            @Schema(description = "선호 카테고리 리스트", example = "[ \"음식\", \"자연\", \"문화\"]")
            List<String> categoryTypeList
    ) {}

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VideoUrlDto {
        @JsonProperty("video_url_list")
        @Schema(description = "받은 유튜브 링크 리스트")
        private List<String> videoUrlList = new ArrayList<>();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegionDto {
        @JsonProperty("region")
        @Schema(description = "여행 지역", example = "경주")
        private String region;
    }

    public record TitleDto (
            @Schema(description = "생성된 여행 제목", example = "길쑤니 빵투어")
            String title
    ) {}
}
