package com.gills_dream.domain.place.service;

import com.gills_dream.domain.place.entity.Place;
import com.gills_dream.domain.place.record.PlacePair;
import com.gills_dream.domain.plan.dto.PlanRequestDto;
import com.gills_dream.domain.plan.entity.Plan;
import com.gills_dream.domain.plan.entity.PlanPlace;

import java.util.List;

public interface PlaceService {
    List<PlanPlace> createList(Plan plan, List<PlanRequestDto.StayPlaceDto> stayPlaceDtoList, List<PlanRequestDto.TravelPlaceDto> travelPlaceDtoList);
    List<PlanPlace> createList(Plan plan, PlacePair placePair);
}
