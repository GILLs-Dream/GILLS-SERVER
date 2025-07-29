package com.gills_dream.global.map.openRouteService.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "OpenRouteService 자전거 경로 요청 DTO")
public record ORSRouteReq(

        @Schema(description = "출발지 경도 (홍대입구역)", example = "126.9236549")
        double startLon,

        @Schema(description = "출발지 위도 (홍대입구역)", example = "37.5567704")
        double startLat,

        @Schema(description = "도착지 경도 (홍익대학교 서울캠퍼스 정문)", example = "126.9243216")
        double endLon,

        @Schema(description = "도착지 위도 (홍익대학교 서울캠퍼스 정문)", example = "37.5527986")
        double endLat
) {}