package com.gills_dream.global.map.kakao.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "다중 경유지 길찾기 요청 DTO")
public record MobilityRouteReq(
        @Schema(description = "출발지 정보")
        Place origin,

        @Schema(description = "목적지 정보")
        Place destination,

        @Schema(description = "경유지 리스트, 최대 30개까지 허용")
        List<Waypoint> waypoints,

        @Schema(description = "경로 탐색 우선순위 옵션", example = "RECOMMEND", defaultValue = "RECOMMEND")
        String priority,

        @Schema(description = "경로 탐색 제한 옵션", example = "[\"ferries\", \"motorway\"]")
        List<String> avoid,

        @Schema(description = "도로 통제 정보 반영 옵션 (0, 1, 2)", defaultValue = "0")
        Integer roadevent,

        @Schema(description = "대안 경로 제공 여부", defaultValue = "false")
        Boolean alternatives,

        @Schema(description = "상세 도로 정보 제공 여부", defaultValue = "false")
        Boolean road_details,

        @Schema(description = "차종 (기본값: 1)", defaultValue = "1")
        Integer car_type,

        @Schema(description = "차량 유종 정보 (GASOLINE, DIESEL, LPG)", defaultValue = "GASOLINE")
        String car_fuel,

        @Schema(description = "하이패스 장착 여부", defaultValue = "false")
        Boolean car_hipass,

        @Schema(description = "요약 정보만 제공 여부", defaultValue = "false")
        Boolean summary
) {
    // 출발지, 목적지, 경유지 공통 속성 정의
    @Schema(description = "출발지 또는 목적지 정보")
    public record Place(
            @Schema(description = "장소 이름", example = "출발지 이름")
            String name,

            @Schema(description = "X 좌표 (경도)", example = "127.11024293202674")
            double x,

            @Schema(description = "Y 좌표 (위도)", example = "37.394348634049784")
            double y,

            @Schema(description = "출발지 초기 진행 방향 (0~360), 선택 사항", example = "270")
            Integer angle
    ) {}

    @Schema(description = "경유지 정보")
    public record Waypoint(
            @Schema(description = "경유지 이름", example = "name0")
            String name,

            @Schema(description = "X 좌표 (경도)", example = "127.11341936045922")
            double x,

            @Schema(description = "Y 좌표 (위도)", example = "37.39639094915999")
            double y
    ) {}
}