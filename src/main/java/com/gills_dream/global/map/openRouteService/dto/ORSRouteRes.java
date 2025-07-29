package com.gills_dream.global.map.openRouteService.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "OpenRouteService 자전거 경로 응답 DTO")
@JsonIgnoreProperties(ignoreUnknown = true)
public record ORSRouteRes(
        @Schema(description = "경로 목록")
        List<Feature> features
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Feature(
            @Schema(description = "경로 유형")
            String type,

            @Schema(description = "경로 속성 정보")
            Properties properties,

            @Schema(description = "경로 선 정보")
            Geometry geometry
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Properties(
            @Schema(description = "경로 구간 목록")
            List<Segment> segments
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Segment(
            @Schema(description = "이동 거리 (m)", example = "1152.7")
            double distance,

            @Schema(description = "이동 시간 (초)", example = "247.1")
            double duration,

            @Schema(description = "단계별 안내 목록")
            List<Step> steps
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Step(
            @Schema(description = "안내 문구", example = "Turn left onto Roonstraße")
            String instruction,

            @Schema(description = "거리 (m)", example = "53.4")
            double distance,

            @Schema(description = "소요 시간 (초)", example = "10.7")
            double duration,

            @Schema(description = "도로명", example = "Roonstraße")
            String name,

            @Schema(description = "지시 유형 코드")
            int type
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Geometry(
            @Schema(description = "좌표 목록 (경도, 위도 순)")
            List<List<Double>> coordinates,

            @Schema(description = "지오메트리 타입", example = "LineString")
            String type
    ) {}
}