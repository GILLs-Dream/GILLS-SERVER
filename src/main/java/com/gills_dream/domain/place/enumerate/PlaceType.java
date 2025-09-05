package com.gills_dream.domain.place.enumerate;

import com.gills_dream.global.apiPayload.code.status.ErrorStatus;
import com.gills_dream.global.apiPayload.exception.GeneralException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum PlaceType {
    // Todo: 여행지, 식당, 카페, 숙소로 세분화할지 의논
    TOUR("여행지"),
    ACCOMMODATION("숙소"),
    RESTAURANT("식당"),
    CAFE("카페");

    private final String description;

    public static PlaceType fromDescription(String description) {
        return Arrays.stream(values())
                .filter(type -> type.description.equals(description))
                .findAny().orElseThrow(() -> new GeneralException(ErrorStatus.NO_SUCH_PLACE_TYPE));
    }
}
