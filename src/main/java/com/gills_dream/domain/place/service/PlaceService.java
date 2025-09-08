package com.gills_dream.domain.place.service;

import com.gills_dream.domain.map.dto.MapRouteByDayRes;
import com.gills_dream.domain.place.dto.PlaceAddedDto;
import com.gills_dream.domain.place.dto.PlaceCacheWithContentId;
import com.gills_dream.domain.place.dto.PlaceSearchReq;
import com.gills_dream.domain.place.dto.PlaceSearchRes;
import com.gills_dream.domain.place.record.PlacePair;
import com.gills_dream.domain.plan.dto.PlanRequestDto;
import com.gills_dream.domain.plan.entity.Plan;

import java.util.List;

public interface PlaceService {
    void createList(Plan plan, PlanRequestDto.DestinationDto destinationDtoList);
    PlaceSearchRes choosePlace(PlaceSearchReq req);
    List<PlaceCacheWithContentId> searchPlaces(Plan plan, PlacePair placePair);
    List<PlaceAddedDto> addToRedis(Plan plan, List<PlaceCacheWithContentId> placeCacheList);
    void updatePlaces(Plan plan, MapRouteByDayRes generated);
}
