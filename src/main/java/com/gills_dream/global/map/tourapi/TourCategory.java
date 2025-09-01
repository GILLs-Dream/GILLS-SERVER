package com.gills_dream.global.map.tourapi;

public enum TourCategory {
    all(null, "전체"),
    attraction("12", "관광지"),
    culture("14", "문화시설"),
    festival("15", "행사/공연/축제"),
    course("25", "여행코스"),
    leisure("28", "레포츠"),
    lodging("32", "숙박"),
    shopping("38", "쇼핑"),
    food("39", "음식점");

    private final String contentTypeId;
    private final String koreanName;

    TourCategory(String contentTypeId, String koreanName) {
        this.contentTypeId = contentTypeId;
        this.koreanName = koreanName;
    }

    public String getContentTypeId() { return contentTypeId; }
    public String getKoreanName() { return koreanName; }

    public static String resolveContentTypeId(String keyword) {
        if (keyword == null || keyword.isBlank()) return null;
        String k = keyword.trim();
        // 숫자 그대로 들어오면 바로 사용
        if (k.chars().allMatch(Character::isDigit)) return k;
        for (TourCategory c : values()) {
            if (c.name().equalsIgnoreCase(k) || c.koreanName.equals(k)) {
                return c.contentTypeId;
            }
        }
        return null;
    }

    /**
     * 12 → attraction
     */
    public static String fromContentTypeId(String id) {
        if (id == null || id.isBlank()) return null;
        for (TourCategory c : values()) {
            if (id.equals(c.contentTypeId)) return String.valueOf(c.name().charAt(0)).toLowerCase() + c.name().substring(1);
        }
        return null;
    }
}