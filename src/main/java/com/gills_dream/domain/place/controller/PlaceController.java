package com.gills_dream.domain.place.controller;

import com.gills_dream.domain.place.dto.PlaceSearchReq;
import com.gills_dream.domain.place.dto.PlaceSearchRes;
import com.gills_dream.domain.place.service.PlaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/place")
@RequiredArgsConstructor
@Tag(name = "Place", description = "장소 API")
public class PlaceController {

    private final PlaceService placeService;

    @Operation(summary = "장소 선택", description = "사용자가 선택한 장소를 저장하고, 관련된 장소 정보를 반환합니다.")
    @PostMapping("/choose")
    public PlaceSearchRes choosePlace(@RequestBody PlaceSearchReq req) {
        return placeService.choosePlace(req);
    }

}
