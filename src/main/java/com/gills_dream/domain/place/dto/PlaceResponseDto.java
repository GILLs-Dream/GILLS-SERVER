package com.gills_dream.domain.place.dto;

public class PlaceResponseDto {
    public record TourApiResultDto (
            String title,
            Double mapx,
            Double mapy,
            String firstimage,
            String overview
    ) {}
}
