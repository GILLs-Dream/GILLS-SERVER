package com.gills_dream.domain.map;

import com.gills_dream.global.map.tourapi.TourApiClient;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.gills_dream.global.map.tourapi.TourKeywordSearchRes;

import java.util.List;

@RestController
@RequestMapping("/v1/map")
@RequiredArgsConstructor
@Tag(name = "Map", description = "지도 API")
public class MapController {
    private final TourApiClient tourApiClient;
    @GetMapping("/tour-api/search")
    public List<TourKeywordSearchRes> searchTourPlaces(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "12") String category,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int numOfRows
    ) {
        return tourApiClient.searchByKeyword(keyword, category, pageNo, numOfRows);
    }
}
