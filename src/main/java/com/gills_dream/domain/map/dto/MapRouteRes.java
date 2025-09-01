package com.gills_dream.domain.map.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import java.util.List;

@Builder
public record MapRouteRes(
        @Schema(description = "총 거리(m)", example = "12345.67")
        double distance,
        @Schema(description = "총 소요 시간(초)", example = "3600")
        double duration,
        @Schema(description = "폴리라인(경로 좌표 문자열)", example = "abcdEfghIjklMnopQrstUvwxyZ")
        String polyline,
        @Schema(description = "세부 경로 정보")
        List<Leg> legs
) {
    @Builder
    public record Leg(
            @Schema(description = "출발지명", example = "경복궁")
            String startName,
            @Schema(description = "출발지 위도", example = "37.579617")
            double startLat,
            @Schema(description = "출발지 경도", example = "126.977041")
            double startLng,
            @Schema(description = "도착지명", example = "홍대입구역")
            String endName,
            @Schema(description = "도착지 위도", example = "37.557192")
            double endLat,
            @Schema(description = "도착지 경도", example = "126.925381")
            double endLng,
            @Schema(description = "이동 수단", example = "WALK/CAR/BUS/SUBWAY/BICYCLE")
            String mode,
            double distance,
            @Schema(description = "소요 시간(초)", example = "1800")
            double duration
    ) {}
}