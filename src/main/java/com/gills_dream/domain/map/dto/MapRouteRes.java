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
            @Schema(description = "출발지 정보")
            PlaceInfo startPlace,
            @Schema(description = "도착지명", example = "홍대입구역")
            String endName,
            @Schema(description = "도착지 위도", example = "37.557192")
            double endLat,
            @Schema(description = "도착지 경도", example = "126.925381")
            double endLng,
            @Schema(description = "도착지 정보")
            PlaceInfo endPlace,
            @Schema(description = "이동 수단", example = "WALK/CAR/TRANSPORT/BICYCLE")
            String mode,
            double distance,
            @Schema(description = "소요 시간(초)", example = "1800")
            double duration
    ) {
        public record PlaceInfo(
                @Schema(description = "플랫폼", example = "KAKAO")
                String platform,
                @Schema(description = "컨텐츠ID", example = "1234")
                String contentId,
                @Schema(description = "장소 타입" , example = "TOUR")
                String placeType,
                @Schema(description = "상세 설명 URL", example = "http://example.com/details")
                String detailUrl,
                @Schema(description = "이미지 URL", example = "http://example.com/image.jpg")
                String imageUrl,
                Period period
        ) {
            public record Period(
                    @Schema(description = "시작일", example = "2025-09-05")
                    String startedAt,
                    @Schema(description = "종료일", example = "2025-09-07")
                    String finishedAt,
                    @Schema(description = "체류 일수", example = "2")
                    Integer duration,
                    @Schema(description = "몇일차", example = "1")
                    Integer dayOrder
            ) {}
        }
    }
}