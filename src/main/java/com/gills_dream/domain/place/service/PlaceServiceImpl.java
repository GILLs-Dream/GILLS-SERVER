package com.gills_dream.domain.place.service;

import com.gills_dream.domain.place.dto.PlaceSearchReq;
import com.gills_dream.domain.place.dto.PlaceSearchRes;
import com.gills_dream.domain.place.entity.Place;
import com.gills_dream.domain.place.enumerate.PlaceType;
import com.gills_dream.domain.place.repository.PlaceRepository;
import com.gills_dream.domain.place.vo.PlaceCache;
import com.gills_dream.domain.place.vo.PlacePeriod;
import com.gills_dream.domain.plan.dto.PlanRequestDto;
import com.gills_dream.domain.plan.entity.Plan;
import com.gills_dream.domain.plan.entity.PlanPlace;
import com.gills_dream.domain.plan.repository.PlanPlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceServiceImpl implements PlaceService {
    private final PlaceRepository placeRepository;
    private final PlanPlaceRepository planPlaceRepository;
    private final PlaceCacheService placeCacheService;

    @Override
    @Transactional
    public List<PlanPlace> createList(Plan plan, List<PlanRequestDto.StayPlaceDto> stayPlaceDtoList, List<PlanRequestDto.TravelPlaceDto> travelPlaceDtoList) {
        List<Place> placeList = new ArrayList<>();
        List<PlanPlace> planPlaceList = new ArrayList<>();

        for (PlanRequestDto.StayPlaceDto dto : stayPlaceDtoList) {
            Place place = placeRepository.findByNameAndAddress(dto.placeName(), dto.address())
                    .orElseGet(() -> createPlace(dto.placeName(), dto.address(), dto.description(), dto.img(), PlaceType.ACCOMMODATION));
            PlanPlace planPlace = createPlanPlace(plan, place, createPeriod(dto.startDate(), dto.endDate()));
            placeList.add(place);
            planPlaceList.add(planPlace);
        }

        for (PlanRequestDto.TravelPlaceDto dto : travelPlaceDtoList) {
            Place place = placeRepository.findByNameAndAddress(dto.placeName(), dto.address())
                    .orElseGet(() -> createPlace(dto.placeName(), dto.address(), dto.description(), dto.img(), PlaceType.TRAVEL_DESTINATION));
            PlanPlace planPlace = createPlanPlace(plan, place, createPeriod(dto.date(), null));
            placeList.add(place);
            planPlaceList.add(planPlace);
        }

        placeRepository.saveAll(placeList);
        planPlaceRepository.saveAll(planPlaceList);

        return planPlaceList;
    }

    private Place createPlace(String name, String address, String desc, String img, PlaceType type) {
        Place place = Place.builder()
                .name(name)
                .address(address)
                .description(desc)
                .placeType(type)
                .build();
        place.addImage(img);
        return place;
    }

    private PlacePeriod createPeriod(LocalDate start, LocalDate end) {
        Long duration = (start != null && end != null) ? ChronoUnit.DAYS.between(start, end) : null;
        return PlacePeriod.builder()
                .startedAt(start)
                .finishedAt(end)
                .duration(duration)
                .build();
    }

    private PlanPlace createPlanPlace(Plan plan, Place place, PlacePeriod period) {
        PlanPlace planPlace = PlanPlace.builder()
                .plan(plan)
                .place(place)
                .period(period)
                .build();
        place.getPlanPlaces().add(planPlace);
        return planPlace;
    }

    @Override
    public PlaceSearchRes choosePlace(PlaceSearchReq req) {
        return placeCacheService.save(
                String.valueOf(req.platform()),
                req.contentId(),
                req.planId(),
                PlaceCache.builder()
                        .name(req.name())
                        .lat(req.lat())
                        .lng(req.lng())
                        .imageUrl(req.imageUrl())
                        .detailUrl(req.detailUrl())
                        .period(req.period() == null ? null : PlaceCache.Period.builder()
                                .nights(req.period().nights())
                                .dayOrder(req.period().dayOrder())
                                .build())
                        .build()
        );
    }
}
