package com.gills_dream.domain.place.service;

import com.gills_dream.domain.place.dto.PlaceResponseDto;
import com.gills_dream.domain.place.entity.Place;
import com.gills_dream.domain.place.enumerate.PlaceType;
import com.gills_dream.domain.place.record.PlacePair;
import com.gills_dream.domain.place.repository.PlaceRepository;
import com.gills_dream.domain.place.vo.PlacePeriod;
import com.gills_dream.domain.plan.dto.PlanRequestDto;
import com.gills_dream.domain.plan.entity.Plan;
import com.gills_dream.domain.plan.entity.PlanPlace;
import com.gills_dream.domain.plan.repository.PlanPlaceRepository;
import com.gills_dream.global.map.tmap.TmapPoiSearchClient;
import com.gills_dream.global.map.tmap.dto.PoiSearchRes;
import com.gills_dream.global.map.tourapi.TourApiClient;
import com.gills_dream.global.map.tourapi.TourKeywordSearchRes;
import com.gills_dream.global.map.tourapi.TourPlaceDetailRes;
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
    private final TmapPoiSearchClient tmapPoiSearchClient;
    private final TourApiClient tourApiClient;


    @Override
    @Transactional
    public List<PlanPlace> createList(Plan plan, List<PlanRequestDto.StayPlaceDto> stayPlaceDtoList, List<PlanRequestDto.TravelPlaceDto> travelPlaceDtoList) {
        List<Place> placeList = new ArrayList<>();
        List<PlanPlace> planPlaceList = new ArrayList<>();

        for (PlanRequestDto.StayPlaceDto dto : stayPlaceDtoList) {
            Place place = placeRepository.findByNameAndLocXAndLocYRounded(dto.placeName(), dto.locX(), dto.locY())
                    .orElseGet(() -> createPlace(dto.placeName(), dto.locX(), dto.locY(), dto.description(), dto.img(), PlaceType.ACCOMMODATION));
            PlanPlace planPlace = createPlanPlace(plan, place, createPeriod(dto.startDate(), dto.endDate()));
            placeList.add(place);
            planPlaceList.add(planPlace);
        }

        for (PlanRequestDto.TravelPlaceDto dto : travelPlaceDtoList) {
            Place place = placeRepository.findByNameAndLocXAndLocYRounded(dto.placeName(), dto.locX(), dto.locY())
                    .orElseGet(() -> createPlace(dto.placeName(), dto.locX(), dto.locY(), dto.description(), dto.img(), PlaceType.TRAVEL_DESTINATION));
            PlanPlace planPlace = createPlanPlace(plan, place, createPeriod(dto.date(), null));
            placeList.add(place);
            planPlaceList.add(planPlace);
        }

        placeRepository.saveAll(placeList);
        planPlaceRepository.saveAll(planPlaceList);

        return planPlaceList;
    }

    @Override
    @Transactional
    public List<PlanPlace> createList(Plan plan, PlacePair placePair) {
        List<Place> placeList = new ArrayList<>();
        List<PlanPlace> planPlaceList = new ArrayList<>();

        // 장소 검색 -> 장소 이름, locX, locY 추출
        // 이미 등록된 장소인지 확인 후 없으면 새로 생성
        for (String travel : placePair.travelDestList()) {
            List<PoiSearchRes.Poi> pois = tmapPoiSearchClient.searchPois(travel, 1);
            if (!pois.isEmpty()) {
                PoiSearchRes.Poi poi = pois.getFirst();
                PlaceResponseDto.TourApiResultDto dto = getPlaceDetail(poi);

                Place place = placeRepository.findByNameAndLocXAndLocYRounded(travel, Double.parseDouble(poi.frontLon()), Double.parseDouble(poi.frontLat()))
                        .orElseGet(() -> createPlace(travel, Double.parseDouble(poi.frontLon()), Double.parseDouble(poi.frontLat()), dto.overview(), dto.firstimage(), PlaceType.TRAVEL_DESTINATION));
                PlanPlace planPlace = createPlanPlace(plan, place, createPeriod(null, null));
                placeList.add(place);
                planPlaceList.add(planPlace);
            }
        }

        for (String accom : placePair.accomList()) {
            List<PoiSearchRes.Poi> pois = tmapPoiSearchClient.searchPois(accom, 1);
            if (!pois.isEmpty()) {
                PoiSearchRes.Poi poi = pois.getFirst();
                PlaceResponseDto.TourApiResultDto dto = getPlaceDetail(poi);

                Place place = placeRepository.findByNameAndLocXAndLocYRounded(accom, Double.parseDouble(poi.frontLon()), Double.parseDouble(poi.frontLat()))
                        .orElseGet(() -> createPlace(accom, Double.parseDouble(poi.frontLon()), Double.parseDouble(poi.frontLat()), dto.overview(), dto.firstimage(), PlaceType.ACCOMMODATION));
                PlanPlace planPlace = createPlanPlace(plan, place, createPeriod(null, null));
                placeList.add(place);
                planPlaceList.add(planPlace);
            }
        }

        placeRepository.saveAll(placeList);
        planPlaceRepository.saveAll(planPlaceList);

        return planPlaceList;
    }

    private PlaceResponseDto.TourApiResultDto getPlaceDetail(PoiSearchRes.Poi poi) {
        List<TourKeywordSearchRes> resultList = tourApiClient.searchByKeyword(poi.name(), "", 1, 10);
        TourKeywordSearchRes result = null;
        TourPlaceDetailRes detResult = null;

        if (!resultList.isEmpty()) {
            result = resultList.stream().filter(res -> res.title().equals(poi.name())).findFirst()
                    .orElse(null);
            if (result != null) {
                 detResult = tourApiClient.getPlaceDetail(result.contentId());
            }
        }

        return new PlaceResponseDto.TourApiResultDto(
                result != null ? result.title() : "",
                result != null ? Double.parseDouble(result.mapx()) : null,
                result != null ? Double.parseDouble(result.mapy()) : null,
                result != null ? result.firstimage() : "",
                detResult != null ? detResult.overview() : ""
        );
    }

    private Place createPlace(String name, double locX, double locY, String desc, String img, PlaceType type) {
        Place place = Place.builder()
                .name(name)
                .locX(locX)
                .locY(locY)
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
}
