package com.gills_dream.domain.place.dto;

import com.gills_dream.domain.place.vo.PlaceCache;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record PlaceCacheWithContentId(
        @Schema(description = "content id", example = "1234")
        String contentId,
        @Schema(description = "PlaceCache 리스트")
        PlaceCache placeCache
) {}
