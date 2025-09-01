package com.gills_dream.domain.map.enumerate;


import com.gills_dream.global.apiPayload.code.status.ErrorStatus;
import com.gills_dream.global.apiPayload.exception.GeneralException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum ContentType {
    KAKAO("카카오"),
    TOUR_API("투어"),
    VIDEO("영상");

    private final String description;

    public static ContentType fromDescription(String description) {
        return Arrays.stream(values())
                .filter(type -> type.description.equals(description))
                .findAny().orElseThrow(() -> new GeneralException(ErrorStatus.NO_SUCH_MAP));
    }
}
