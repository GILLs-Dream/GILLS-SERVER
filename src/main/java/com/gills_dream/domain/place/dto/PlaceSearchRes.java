package com.gills_dream.domain.place.dto;

import lombok.Builder;

@Builder
public record PlaceSearchRes(
        String contentId,
        Long planId,
        String platform
) {}