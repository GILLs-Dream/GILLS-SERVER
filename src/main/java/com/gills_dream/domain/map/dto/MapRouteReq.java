package com.gills_dream.domain.map.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

@Builder
public record MapRouteReq(
        List<Location> locations
) {
    @Builder
    @Schema(description = "장소 좌표 + 이름")
    public record Location(
            @Schema(description = "위도", example = "37.579617")
            double lat,

            @Schema(description = "경도", example = "126.977041")
            double lng,

            @Schema(description = "장소명", example = "경복궁")
            String name
    ){
        public static Location of(double lat, double lng, String name) {
            return Location.builder()
                    .lat(lat)
                    .lng(lng)
                    .name(name)
                    .build();
        }
    }
    public static MapRouteReq of(List<Location> locations) {
        return MapRouteReq.builder()
                .locations(locations)
                .build();
    }
}
/*
{
  "locations": [
    {
      "lat": "37.579617",
      "lng": "126.977041",
      "name": "경복궁"
    },
    {
      "lat": "37.557192",
      "lng": "126.925381",
      "name": "홍대입구역"
    },
    {
      "lat": "37.5219",
      "lng": "126.9244",
      "name": "여의도역"
    }
  ]
}
 */