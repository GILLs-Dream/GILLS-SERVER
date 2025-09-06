package com.gills_dream.domain.map.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import java.util.List;

@Builder
public record MapRouteByDayRes(
        @Schema(description = "DATED 또는 INDEXED")
        Mode mode,
        @Schema(description = "일자별 경로")
        List<Day> days
) {
    public enum Mode { DATED, INDEXED }

    @Builder
    public record Day(
            @Schema(description = "DATED 모드에서만 사용", example = "2025-09-01")
            String date,     // null 허용

            @Schema(description = "INDEXED 모드에서만 사용", example = "1")
            Integer index,   // null 허용

            @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
            @Schema(accessMode = Schema.AccessMode.WRITE_ONLY)
            List<MapRouteReq.Location> locations,

            @Schema(description = "해당 일자의 경로 결과(거리/시간/legs). 0개 또는 1개 이상 가능")
            List<MapRouteRes> routes
    ) {}
}