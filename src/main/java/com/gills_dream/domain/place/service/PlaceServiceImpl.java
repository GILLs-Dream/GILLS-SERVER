package com.gills_dream.domain.place.service;

import com.gills_dream.domain.place.entity.Place;
import com.gills_dream.domain.place.enumerate.PlaceType;
import com.gills_dream.domain.place.repository.PlaceRepostiory;
import com.gills_dream.domain.place.vo.PlacePeriod;
import com.gills_dream.domain.plan.dto.PlanRequestDto;
import com.gills_dream.domain.plan.entity.Plan;
import com.gills_dream.domain.plan.entity.PlanPlace;
import com.gills_dream.domain.plan.repository.PlanPlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceServiceImpl implements PlaceService {
    private final PlaceRepostiory placeRepostiory;
    private final PlanPlaceRepository planPlaceRepository;

    @Override
    @Transactional
    public List<PlanPlace> createList(Plan plan, List<PlanRequestDto.StayPlaceDto> stayPlaceDtoList, List<PlanRequestDto.TravelPlaceDto> travelPlaceDtoList) {
        List<Place> placeList = new ArrayList<>();
        List<PlanPlace> planPlaceList = new ArrayList<>();
        for (PlanRequestDto.StayPlaceDto stayPlaceDto : stayPlaceDtoList) {
            Place newPlace = Place.builder()
                    .name(stayPlaceDto.placeName())
                    .address(stayPlaceDto.address())
                    .description(stayPlaceDto.description())
                    .placeType(PlaceType.ACCOMMODATION).build();
            newPlace.addImage(stayPlaceDto.img());

            PlanPlace newPlanPlace = PlanPlace.builder()
                    .plan(plan)
                    .place(newPlace)
                    .period(PlacePeriod.builder()
                            .startedAt(stayPlaceDto.startDate())
                            .finishedAt(stayPlaceDto.endDate())
                            .duration(ChronoUnit.DAYS.between(stayPlaceDto.startDate(), stayPlaceDto.endDate())).build())
                    .build();

            placeList.add(newPlace);
            planPlaceList.add(newPlanPlace);
            newPlace.getPlanPlaces().add(newPlanPlace);
        }

        for (PlanRequestDto.TravelPlaceDto travelPlaceDto : travelPlaceDtoList) {
            Place newPlace = Place.builder()
                    .name(travelPlaceDto.placeName())
                    .address(travelPlaceDto.address())
                    .description(travelPlaceDto.description())
                    .placeType(PlaceType.TRAVEL_DESTINATION).build();
            newPlace.addImage(travelPlaceDto.img());

            PlanPlace newPlanPlace = PlanPlace.builder()
                    .plan(plan)
                    .place(newPlace)
                    .period(PlacePeriod.builder()
                            .startedAt(travelPlaceDto.date()).build())
                    .build();

            placeList.add(newPlace);
            planPlaceList.add(newPlanPlace);
            newPlace.getPlanPlaces().add(newPlanPlace);
        }

        placeRepostiory.saveAll(placeList);
        planPlaceRepository.saveAll(planPlaceList);

        return planPlaceList;
    }
}
