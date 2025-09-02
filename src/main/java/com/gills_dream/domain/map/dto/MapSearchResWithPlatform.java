package com.gills_dream.domain.map.dto;

import com.gills_dream.domain.place.enumerate.Platform;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
public record MapSearchResWithPlatform(
        @Schema(description = "플랫폼 [KAKAO, TOUR]", example = "KAKAO")
        Platform platform,

        @Schema(description = "MapSearchRes")
        List<MapSearchRes> mapSearchResList
) {}
