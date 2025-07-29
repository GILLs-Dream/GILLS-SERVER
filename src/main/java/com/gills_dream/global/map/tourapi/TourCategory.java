package com.gills_dream.global.map.tourapi;

public enum TourCategory {
    all(null),
    attraction("12"),    // 관광지
    culture("14"),       // 문화시설
    festival("15"),      // 축제/행사
    course("25"),        // 여행코스
    leisure("28"),       // 레포츠
    lodging("32"),       // 숙박
    shopping("38"),      // 쇼핑
    food("39");          // 음식점

    private final String contentTypeId;

    TourCategory(String contentTypeId) {
        this.contentTypeId = contentTypeId;
    }

    public String getContentTypeId() {
        return contentTypeId;
    }

    public static String resolveContentTypeId(String keyword) {
        try {
            return TourCategory.valueOf(keyword.toLowerCase()).getContentTypeId();
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}