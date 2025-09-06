package com.gills_dream.domain.map.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
public record MapRouteReq(
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
            Period period
    ){
        public static Location of(double lat, double lng, String name, String placeType, Period period) {
            return Location.builder()
                    .lat(lat)
                    .lng(lng)
                    .name(name)
                    .placeType(placeType)
                    .period(period)
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
            Integer duration
    ) {
        public static Period of(String startedAt, String finishedAt, Integer duration) {
            return Period.builder()
                    .startedAt(startedAt)
                    .finishedAt(finishedAt)
                    .duration(duration)
                    .build();
        }
    }

    public static MapRouteReq of(List<Location> locations) {
        return MapRouteReq.builder()
                .locations(locations)
                .build();
    }
}