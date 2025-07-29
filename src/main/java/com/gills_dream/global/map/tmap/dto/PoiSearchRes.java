package com.gills_dream.global.map.tmap.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record PoiSearchRes(
        @JsonProperty("searchPoiInfo")
        SearchPoiInfo searchPoiInfo
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SearchPoiInfo(
            Pois pois
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Pois(
            List<Poi> poi
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Poi(
            String id,
            String name,
            String frontLat,
            String frontLon
    ) {}
}