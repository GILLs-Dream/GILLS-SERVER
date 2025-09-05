package com.gills_dream.domain.place.dto;

import lombok.Builder;

@Builder
public record PlaceAddedDto(
        String contentId,
        String name
) {}
