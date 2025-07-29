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
    public static class ProcessYoutubeDto {
        @Schema(description = "유튜브 영상에서 추출된 장소 리스트")
        private List<String> placeList;
    }
}
