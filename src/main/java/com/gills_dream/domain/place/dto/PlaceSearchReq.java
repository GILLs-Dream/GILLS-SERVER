package com.gills_dream.domain.place.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
@Builder
public record PlaceSearchReq(
        @Schema(description = "여행 계획 ID", example = "1")
        Long planId,
        @Schema(description = "contentId", example = "2773715")
        String contentId,
        @Schema(description = "플랫폼", example = "TOURAPI")
        String platform,
        @Schema(description = "장소 타입" , example = "TOUR")
        String placeType,
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
        String imageUrl,
        @Schema(description = "숙박 정보 (숙박이 아닌 경우 null)", example = """
                {
                  "nights": "2",
                  "dayOrder": "1"
                }
                """)
        Period period
) {
    public record Period(
            @Schema(description = "숙박 일수 (n박)", example = "2")
            String nights,

            @Schema(description = "여행 며칠째 (Day 순서)", example = "1")
            String dayOrder
    ) {}
}

