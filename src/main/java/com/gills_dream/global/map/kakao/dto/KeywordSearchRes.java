package com.gills_dream.global.map.kakao.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record KeywordSearchRes(
        Meta meta,
        @Schema(description = "검색된 장소 목록")
        List<Document> documents
) {
    public record Meta(
            @Schema(description = "검색된 장소의 총 개수")
            int total_count,
            @Schema(description = "현재 페이지에서 검색된 장소의 개수")
            int pageable_count,
            @Schema(description = "현재 페이지 번호")
            boolean is_end
    ) {}

    public record Document(
            @Schema(description = "장소 ID")
            String id,
            @Schema(description = "장소 이름")
            String place_name,
            @Schema(description = "장소 카테고리 정보")
            String category_name,
            @Schema(description = "장소 전화번호")
            String phone,
            @Schema(description = "장소 주소")
            String address_name,
            @Schema(description = "도로명 주소")
            String road_address_name,
            @Schema(description = "장소의 X 좌표 (경도)")
            String x,
            @Schema(description = "장소의 Y 좌표 (위도)")
            String y,
            @Schema(description = "장소의 상세 페이지 URL")
            String place_url
    ) {}
}