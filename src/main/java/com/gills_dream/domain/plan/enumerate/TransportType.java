package com.gills_dream.domain.plan.enumerate;

import com.gills_dream.global.apiPayload.code.status.ErrorStatus;
import com.gills_dream.global.apiPayload.exception.GeneralException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum TransportType {
    BUS("버스"),
    CAR("자동차"),
    WALK("도보"),
    BICYCLE("자전거"),
    TRANSPORT("대중교통");
    private final String description;

    public static TransportType fromDescription(String description) {
        return Arrays.stream(values())
                .filter(type -> type.description.equals(description))
                .findAny().orElseThrow(() -> new GeneralException(ErrorStatus.NO_SUCH_CATEGORY));
    }
}
