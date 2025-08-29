package com.gills_dream.global.map.tmap.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
@Builder
@Schema(description = "보행자 경로 안내 요청 DTO (Tmap API 호환)")
public record PedestrianRouteReq(

        @Schema(description = "출발지 X좌표", example = "126.977041")
        double startX,

        @Schema(description = "출발지 Y좌표", example = "37.579617")
        double startY,

        @Schema(description = "출발지 명칭", example = "경복궁")
        String startName,

        @Schema(description = "목적지 X좌표", example = "126.925381")
        double endX,

        @Schema(description = "목적지 Y좌표", example = "37.557192")
        double endY,

        @Schema(description = "목적지 명칭", example = "홍대입구역")
        String endName,

        @Schema(description = "요청 좌표계", defaultValue = "WGS84GEO")
        String reqCoordType,

        @Schema(description = "응답 좌표계", defaultValue = "WGS84GEO")
        String resCoordType,

        @Schema(description = "경로 탐색 옵션 (0:추천, 10:최단 등)", defaultValue = "0")
        String searchOption
) {
        public static PedestrianRouteReq of(double startX, double startY, String startName,
                                            double endX, double endY, String endName) {
                return new PedestrianRouteReq(
                        startX, startY, startName,
                        endX, endY, endName,
                        "WGS84GEO", "WGS84GEO", "0"
                );
        }
}