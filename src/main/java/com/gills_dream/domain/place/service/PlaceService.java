package com.gills_dream.domain.place.service;

import com.gills_dream.domain.place.dto.PlaceSearchReq;
import com.gills_dream.domain.place.dto.PlaceSearchRes;
import com.gills_dream.domain.place.entity.Place;
import com.gills_dream.domain.place.record.PlacePair;
import com.gills_dream.domain.plan.dto.PlanRequestDto;
import com.gills_dream.domain.plan.dto.PlanResponseDto;
import com.gills_dream.domain.plan.entity.Plan;
import com.gills_dream.domain.plan.entity.PlanPlace;

import java.util.List;

public interface PlaceService {
    void createList(Plan plan, PlanRequestDto.DestinationDto destinationDtoList);
    PlaceSearchRes choosePlace(PlaceSearchReq req);
    List<PlanPlace> createList(Plan plan, PlacePair placePair);
    void addList(Plan plan, PlanResponseDto.PlanDraftDto dto);
//    void addToRedis(ContentType contentType, Plan plan, PlacePair placePair);
}
