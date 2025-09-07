package com.gills_dream.domain.place.vo;

import com.gills_dream.domain.place.entity.Place;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record PlaceCache(

        @Schema(description = "컨텐츠ID", example = "1234")
        String contentId,

        @Schema(description = "플랫폼", example = "KAKAO")
        String platform,

        @Schema(description = "관광지명", example = "경복궁")
        String name,

        @Schema(description = "장소 타입" , example = "TOUR")
        String placeType,

        @Schema(description = "위도", example = "37.579617")
        String lat,

        @Schema(description = "경도", example = "126.977041")
        String lng,

        @Schema(description = "이미지 URL", example = "http://example.com/image.jpg")
        String imageUrl,

        @Schema(description = "상세 설명 URL", example = "http://example.com/details")
        String detailUrl,

        Period period
) {
        @Builder
        public record Period(
                @Schema(description = "시작일", example = "2025-09-05")
                String startedAt,

                @Schema(description = "종료일", example = "2025-09-07")
                String finishedAt,

                @Schema(description = "체류 일수", example = "2")
                Integer duration,

                @Schema(description = "몇일차", example = "1")
                Integer dayOrder
        ) {}
        public static PlaceCache fromEntity(Place p, PlacePeriod period) {
                return PlaceCache.builder()
                        .contentId(p.getContentId())
                        .platform(p.getPlatform() == null ? null : p.getPlatform().toString())
                        .name(p.getName())
                        .placeType(p.getPlaceType() == null ? null : p.getPlaceType().toString())
                        .lat(p.getLat())
                        .lng(p.getLng())
                        .imageUrl(p.getImages() == null || p.getImages().isEmpty() ? null : p.getImages().get(0)) // ← ; 제거
                        .detailUrl(p.getDetailUrl())
                        .period(period == null ? null : Period.builder()
                                .startedAt(period.getStartedAt() == null ? null : period.getStartedAt().toString())
                                .finishedAt(period.getFinishedAt() == null ? null : period.getFinishedAt().toString())
                                .duration(period.getDuration())
                                .dayOrder(period.getDayOrder())
                                .build())
                        .build();
        }
}