package com.gills_dream.domain.map;

import lombok.Builder;

@Builder
public record MapSearchRes(
        String id,              // 외부 식별자
        String name,            // 장소명
        String category,        // 카테고리 (없으면 null)
        String lat,             // 위도
        String lng,             // 경도
        String roadAddress,     // 도로명 주소 (없으면 null)
        String detailUrl,        // 상세페이지 URL (없으면 null)
        String imageUrl        // 이미지 URL (없으면 null)
) {}
