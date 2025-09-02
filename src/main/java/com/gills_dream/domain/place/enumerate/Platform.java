package com.gills_dream.domain.place.enumerate;

import com.gills_dream.global.apiPayload.code.status.ErrorStatus;
import com.gills_dream.global.apiPayload.exception.GeneralException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum Platform {
    KAKAO("카카오"),
    TOUR("투어 API"),
    VIDEO("영상");

    private final String description;

    public static Platform fromDescription(String description) {
        return Arrays.stream(values())
                .filter(type -> type.description.equals(description))
                .findAny().orElseThrow(() -> new GeneralException(ErrorStatus.NO_SUCH_MAP));
    }
}