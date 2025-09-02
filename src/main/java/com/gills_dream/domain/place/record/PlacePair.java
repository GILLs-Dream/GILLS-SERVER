package com.gills_dream.domain.place.record;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record PlacePair(
        @JsonProperty("tour") List<String> tourList,
        @JsonProperty("accommodation") List<String> accommodationList) {}
