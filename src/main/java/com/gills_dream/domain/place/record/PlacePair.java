package com.gills_dream.domain.place.record;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record PlacePair(
        @JsonProperty("travel") List<String> travelDestList,
        @JsonProperty("accommodation") List<String> accomList) {}
