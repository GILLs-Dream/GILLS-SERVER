package com.gills_dream.domain.place.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record PlaceCache(

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
                @Column(columnDefinition = "DATE")
                LocalDate startedAt,

                @Column(columnDefinition = "DATE")
                LocalDate finishedAt,

                @Column(columnDefinition = "bigint")
                Integer duration
        ) {}
}