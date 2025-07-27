package com.gills_dream.global.map.tmap.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "보행자 경로 안내 응답 DTO (GeoJSON)")
public record PedestrianRouteRes(

        @Schema(description = "GeoJSON 타입", example = "FeatureCollection")
        String type,

        @Schema(description = "경로 정보 Feature 리스트")
        List<Feature> features
) {
    @Schema(description = "GeoJSON Feature 정보")
    public record Feature(
            @Schema(description = "Feature 타입", example = "Feature")
            String type,

            @Schema(description = "형상 정보 (Point 또는 LineString)")
            Geometry geometry,

            @Schema(description = "경로 안내 속성 정보")
            Properties properties
    ) {}

    @Schema(description = "형상 정보")
    public record Geometry(
            @Schema(description = "Geometry 타입", example = "Point")
            String type,

            @Schema(description = "좌표 정보. Point: [x, y], LineString: [[x1, y1], [x2, y2], ...]",
                    example = "[126.92364851900282, 37.556774278906374]")
            Object coordinates
    ) {}

    @Schema(description = "경로 안내 속성 정보")
    public record Properties(
            @Schema(description = "총 거리 (m)", example = "1057")
            Integer totalDistance,

            @Schema(description = "총 소요 시간 (초)", example = "830")
            Integer totalTime,

            @Schema(description = "경로 순번", example = "0")
            Integer index,

            @Schema(description = "포인트 노드 인덱스", example = "0")
            Integer pointIndex,

            @Schema(description = "안내 지점 이름", example = "홍대입구역 8번출구")
            String name,

            @Schema(description = "안내 설명", example = "도착")
            String description,

            @Schema(description = "방면 명칭", example = "")
            String direction,

            @Schema(description = "주변 POI 이름", example = "세븐일레븐")
            String nearPoiName,

            @Schema(description = "주변 POI X 좌표", example = "0.0")
            String nearPoiX,

            @Schema(description = "주변 POI Y 좌표", example = "0.0")
            String nearPoiY,

            @Schema(description = "교차로 이름", example = "홍익대앞")
            String intersectionName,

            @Schema(description = "시설물 타입 (예: 11 = 일반보행자도로)", example = "11")
            String facilityType,

            @Schema(description = "시설물 명칭", example = "횡단보도")
            String facilityName,

            @Schema(description = "회전 타입 코드", example = "200")
            Integer turnType,

            @Schema(description = "포인트 타입 (SP: 출발지, EP: 도착지, GP: 안내점, PP1~PPN: 경유지)", example = "GP")
            String pointType,

            @Schema(description = "구간 거리 (m)", example = "28")
            Integer distance,

            @Schema(description = "구간 시간 (초)", example = "27")
            Integer time,

            @Schema(description = "도로 타입 (21~24)", example = "24")
            Integer roadType,

            @Schema(description = "특화도로 분류 (0~3)", example = "0")
            Integer categoryRoadType,

            @Schema(description = "라인 인덱스", example = "0")
            Integer lineIndex
    ) {}
}
