package com.gills_dream.domain.python.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class PythonResponseDto {
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProcessResultDto {
        private List<VideoResultDto> videoResultDtoList;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VideoResultDto {
        @Schema(description = "비디오 id")
        private String videoId;
        @Schema(description = "비디오 제목")
        private String title;
        @Schema(description = "비디오 설명")
        private String description;
        @Schema(description = "해당 영상에서 추출된 장소 리스트")
        private List<String> placeList;
    }
}
