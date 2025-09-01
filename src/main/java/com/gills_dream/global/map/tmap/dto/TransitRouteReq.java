package com.gills_dream.global.map.tmap.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "TMAP 대중교통 경로 요청 DTO")
public record TransitRouteReq(
        @Schema(description = "출발지 X좌표", example = "127.02479803562213")
        String startX,

        @Schema(description = "출발지 Y좌표", example = "37.504585233865086")
        String startY,

        @Schema(description = "도착지 X좌표", example = "127.03747630119366")
        String endX,

        @Schema(description = "도착지 Y좌표", example = "37.479103923078995")
        String endY,

        @Schema(description = "언어 선택 (0: 국문, 1: 영문)", example = "0", defaultValue = "0")
        Integer lang,

        @Schema(description = "출력 포맷", example = "json", defaultValue = "json")
        String format,

        @Schema(description = "최대 응답 결과 수", example = "1", defaultValue = "10")
        Integer count
) {}