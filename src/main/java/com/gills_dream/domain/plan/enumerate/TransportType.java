package com.gills_dream.domain.plan.enumerate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TransportType {
    BUS("버스"),
    TRAIN("기차"),
    PLANE("비행기"),
    CAR("자동차"),
    WALK("도보"),
    SUBWAY("지하철"),
    SHIP("배"),
    OTHER("기타");
    private final String description;
}
