package com.gills_dream.domain.map.dto;

import com.gills_dream.domain.plan.vo.PlanPeriod;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
public record MapRouteReq(
        String region,
        PlanPeriod planPeriod,
        List<Location> locations
) {
    @Builder
    @Schema(description = "장소 좌표 + 이름 + 체류기간")
    public record Location(
            @Schema(description = "위도", example = "37.579617")
            double lat,

            @Schema(description = "경도", example = "126.977041")
            double lng,

            @Schema(description = "장소명", example = "경복궁")
            String name,

            @Schema(description = "장소 타입", example = "TOUR")
            String placeType,

            @Schema(description = "체류 기간")
            Period period,

            @Schema(description = "장소 ID (nullable)", example = "1234")
            String contentId,

            @Schema(description = "detail URL (nullable)", example = "http://example.com/details")
            String detailUrl,

            @Schema(description = "image URL (nullable)", example = "http://example.com/image.jpg")
            String imageUrl,

            @Schema(description = "플랫폼 (nullable)", example = "KAKAO")
            String platform
    ){
        public static Location of(double lat, double lng, String name, String placeType, Period period, String contentId, String detailUrl, String imageUrl, String platform) {
            return Location.builder()
                    .lat(lat)
                    .lng(lng)
                    .name(name)
                    .placeType(placeType)
                    .period(period)
                    .contentId(contentId)
                    .detailUrl(detailUrl)
                    .imageUrl(imageUrl)
                    .platform(platform)
                    .build();
        }

        public static Location updateContent(Location location, String contentId, String detailUrl, String imageUrl,
                                             String platform, double lat, double lng, String name) {
            return Location.builder()
                    .lat(lat)
                    .lng(lng)
                    .name(name)
                    .placeType(location.placeType)
                    .period(location.period)
                    .contentId(contentId)
                    .detailUrl(detailUrl)
                    .imageUrl(imageUrl)
                    .platform(platform)
                    .build();
        }
    }

    @Builder
    @Schema(description = "장소별 체류 기간")
    public record Period(
            @Schema(description = "시작일", example = "2025-09-05")
            String startedAt,

            @Schema(description = "종료일", example = "2025-09-07")
            String finishedAt,

            @Schema(description = "체류 일수", example = "2")
            Integer duration,

            @Schema(description = "(날짜가 미정인 경우) 여행 몇일차에 갈지", example = "3")
            Integer dayOrder
    ) {
        public static Period of(String startedAt, String finishedAt, Integer duration, Integer dayOrder) {
            return Period.builder()
                    .startedAt(startedAt)
                    .finishedAt(finishedAt)
                    .duration(duration)
                    .dayOrder(dayOrder)
                    .build();
        }
    }

    public static MapRouteReq of(String region, PlanPeriod period, List<Location> locations) {
        return MapRouteReq.builder()
                .region(region)
                .planPeriod(period)
                .locations(locations)
                .build();
    }
}