package com.gills_dream.domain.plan.enumerate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TransportType {
    BUS("버스"),
    CAR("자동차"),
    WALK("도보"),
    SUBWAY("지하철"),
    BICYCLE("자전거");
    private final String description;
}
