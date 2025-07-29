package com.gills_dream.global.map.tmap.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "보행자 경로 안내 요청 DTO (Tmap API 호환)")
public record PedestrianRouteReq(

        @Schema(description = "출발지 X좌표", example = "126.92365493654832") // 홍대입구역
        double startX,

        @Schema(description = "출발지 Y좌표", example = "37.556770374096615") // 홍대입구역
        double startY,

        @Schema(description = "출발지 명칭 (URL 인코딩)", example = "%EC%B6%9C%EB%B0%9C") // 출발
        String startName,

        @Schema(description = "목적지 X좌표", example = "126.92432158129688") // 홍대 정문
        double endX,

        @Schema(description = "목적지 Y좌표", example = "37.55279861528311") // 홍대 정문
        double endY,

        @Schema(description = "목적지 명칭 (URL 인코딩)", example = "%EB%8F%84%EC%B0%A9") // 도착
        String endName,

        @Schema(description = "각도 (0~359)", example = "20", defaultValue = "0")
        Integer angle,

        @Schema(description = "진행 속도 (Km/h)", example = "30", defaultValue = "30")
        Integer speed,

        @Schema(description = "목적지 POI ID", example = "10001")
        String endPoiId,

        @Schema(description = "경유지 목록 (X,Y_형식, 없으면 빈 문자열)", example = "126.92774822,37.55395475_126.92577620,37.55337145")
        String passList,

        @Schema(description = "요청 좌표계 (WGS84GEO | EPSG3857 | KATECH)", defaultValue = "WGS84GEO", example = "WGS84GEO")
        String reqCoordType,

        @Schema(description = "응답 좌표계 (WGS84GEO | EPSG3857 | KATECH)", defaultValue = "WGS84GEO", example = "WGS84GEO")
        String resCoordType,

        @Schema(description = "경로 탐색 옵션 (0:추천, 10:최단 등)", defaultValue = "0", example = "0")
        String searchOption,

        @Schema(description = "정렬 방식 (index | custom)", defaultValue = "index", example = "index")
        String sort

) {}