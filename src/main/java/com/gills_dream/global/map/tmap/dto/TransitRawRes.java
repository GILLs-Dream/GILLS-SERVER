package com.gills_dream.global.map.tmap.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "TMAP 대중교통 전체 응답 객체")
@JsonIgnoreProperties(ignoreUnknown = true)
public record TransitRawRes(
        @Schema(description = "메타데이터")
        @JsonProperty("metaData")
        MetaData metaData

) {
    @Schema(description = "메타데이터")
    public record MetaData(
            @Schema(description = "경로계획 정보")
            @JsonProperty("plan")
            Plan plan
    ) {}

    @Schema(description = "경로계획")
    public record Plan(
            @Schema(description = "대중교통 경로 목록")
            @JsonProperty("itineraries")
            List<TransitRouteRes> itineraries
    ) {
        public record TransitRouteRes(

                @Schema(description = "총 소요 시간 (초)", example = "873")
                int totalTime,

                @Schema(description = "총 이동 거리 (m)", example = "3157")
                int totalDistance,

                @Schema(description = "총 보행 시간 (초)", example = "462")
                int totalWalkTime,

                @Schema(description = "총 보행 거리 (m)", example = "492")
                int totalWalkDistance,

                @Schema(description = "환승 횟수", example = "1")
                int transferCount,

                @Schema(description = "요금 정보")
                Fare fare,

                @Schema(description = "구간별 이동 정보 리스트")
                List<TransitRouteRes.Leg> legs

        ) {
            @Schema(description = "요금 정보")
            @JsonIgnoreProperties(ignoreUnknown = true)
            public record Fare(
                    @Schema(description = "기본 요금 정보")
                    Regular regular
            ) {
                @Schema(description = "기본 요금 정보")
                @JsonIgnoreProperties(ignoreUnknown = true)
                public record Regular(
                        @Schema(description = "총 요금 (원)", example = "3250")
                        int totalFare
                ) {}
            }

            @Schema(description = "경로 구간 정보")
            public record Leg(

                    @Schema(description = "이동 수단 종류 (WALK, BUS, SUBWAY 등)", example = "SUBWAY")
                    String mode,

                    @Schema(description = "구간 거리 (m)", example = "2445")
                    int distance,

                    @Schema(description = "구간 시간 (초)", example = "240")
                    int sectionTime,

                    @Schema(description = "노선명 또는 도보 설명", example = "신분당선")
                    String route,

                    @Schema(description = "출발 정류장 또는 장소명", example = "신논현")
                    String startName,

                    @Schema(description = "도착 정류장 또는 장소명", example = "양재")
                    String endName
            ) {}
        }
    }
}