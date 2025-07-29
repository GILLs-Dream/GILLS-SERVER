package com.gills_dream.domain.plan.enumerate;

import com.gills_dream.global.apiPayload.code.status.ErrorStatus;
import com.gills_dream.global.apiPayload.exception.GeneralException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

// Todo: 정책 정해지면 적용 필요
@Getter
@RequiredArgsConstructor
public enum CategoryType {
    FOOD("음식"),
    NATURE("자연"),
    SHOPPING("쇼핑"),
    CULTURE("문화"),
    ACTIVITY("액티비티"),
    HEALING("힐링"),
    OTHERS("기타");
    private final String description;

    public static CategoryType fromDescription(String description) {
        return Arrays.stream(values())
                .filter(type -> type.description.equals(description))
                .findAny().orElseThrow(() -> new GeneralException(ErrorStatus.NO_SUCH_CATEGORY));
    }
}
