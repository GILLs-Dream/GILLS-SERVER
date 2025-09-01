package com.gills_dream.domain.map.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record MapSearchRes(
        @Schema(description = "contentId", example = "2773715")
        String id,
        @Schema(description = "장소명", example = "경복궁 하남미사점")
        String name,
        @Schema(description = "카테고리", example = "food")
        String category,
        @Schema(description = "위도", example = "37.5629697700")
        String lat,
        @Schema(description = "경도", example = "127.2003009251")
        String lng,
        @Schema(description = "도로명 주소", example = "경기도 하남시 미사대로 464 (망월동)")
        String roadAddress,
        @Schema(description = "상세페이지 URL", example = "http://example.com/details")
        String detailUrl,
        @Schema(description = "이미지 URL", example = "http://tong.visitkorea.or.kr/cms/resource/12/2773812_image2_1.jpeg")
        String imageUrl
) {}
