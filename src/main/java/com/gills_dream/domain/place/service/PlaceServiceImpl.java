package com.gills_dream.domain.place.service;

import com.gills_dream.domain.place.dto.PlaceResponseDto;
import com.gills_dream.domain.place.entity.Place;
import com.gills_dream.domain.place.enumerate.PlaceType;
import com.gills_dream.domain.place.record.PlacePair;
import com.gills_dream.domain.place.repository.PlaceRepository;
import com.gills_dream.domain.place.vo.PlacePeriod;
import com.gills_dream.domain.plan.dto.PlanRequestDto;
import com.gills_dream.domain.plan.dto.PlanResponseDto;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceServiceImpl implements PlaceService {
    private final PlaceRepository placeRepository;
    private final PlanPlaceRepository planPlaceRepository;
    private final TmapPoiSearchClient tmapPoiSearchClient;
    private final TourApiClient tourApiClient;
    private record PoiWithDetail(PoiSearchRes.Poi poi, PlaceResponseDto.TourApiResultDto detail) {}


    @Override
    @Transactional
    public List<PlanPlace> createList(Plan plan, List<PlanRequestDto.StayPlaceDto> stayPlaceDtoList, List<PlanRequestDto.TravelPlaceDto> travelPlaceDtoList) {
        List<Place> placeList = new ArrayList<>();
        List<PlanPlace> planPlaceList = new ArrayList<>();

        for (PlanRequestDto.StayPlaceDto dto : stayPlaceDtoList) {
            Place place = findOrCreatePlace(dto.placeName(), Double.parseDouble(dto.locX()), Double.parseDouble(dto.locY()), dto.description(), dto.img(), PlaceType.ACCOMMODATION, placeList);
            ensureLink(plan, place, createPeriod(dto.startDate(), dto.endDate()), planPlaceList);
        }

        for (PlanRequestDto.TravelPlaceDto dto : travelPlaceDtoList) {
            Place place = findOrCreatePlace(dto.placeName(), Double.parseDouble(dto.locX()), Double.parseDouble(dto.locY()), dto.description(), dto.img(), PlaceType.TRAVEL_DESTINATION, placeList);
            ensureLink(plan, place, createPeriod(dto.date(), null), planPlaceList);
        }

        if (!placeList.isEmpty()) placeRepository.saveAll(placeList);
        if (!planPlaceList.isEmpty()) planPlaceRepository.saveAll(planPlaceList);
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
            PoiWithDetail poiWDet = searchPoiAndDetail(travel);
            if (poiWDet == null) continue;
            Place place = findOrCreatePlace(travel, Double.parseDouble(poiWDet.poi.frontLon()), Double.parseDouble(poiWDet.poi.frontLat()), poiWDet.detail.overview(), poiWDet.detail.firstimage(), PlaceType.TRAVEL_DESTINATION, placeList);
            ensureLink(plan, place, createPeriod(null, null), planPlaceList);
        }

        for (String accom : placePair.accomList()) {
            PoiWithDetail poiWDet = searchPoiAndDetail(accom);
            if (poiWDet == null) continue;
            Place place = findOrCreatePlace(accom, Double.parseDouble(poiWDet.poi.frontLon()), Double.parseDouble(poiWDet.poi.frontLat()), poiWDet.detail.overview(), poiWDet.detail.firstimage(), PlaceType.ACCOMMODATION, placeList);
            ensureLink(plan, place, createPeriod(null, null), planPlaceList);
        }

        if (!placeList.isEmpty()) placeRepository.saveAll(placeList);
        if (!planPlaceList.isEmpty()) planPlaceRepository.saveAll(planPlaceList);
        return planPlaceList;
    }

    @Override
    @Transactional
    public void addList(Plan plan, PlanResponseDto.PlanDraftDto dto) {
        List<Place> placeList = new ArrayList<>();
        List<PlanPlace> planPlaceList = new ArrayList<>();

        Map<String, LocalDate> accStart = new HashMap<>();
        Map<String, LocalDate> accEnd   = new HashMap<>();
        for (PlanResponseDto.DayDraftDto day : dto.draftDtoList()) {
            if (day.accommodationList() == null) continue;
            for (PlanResponseDto.PlaceDraftDto p : day.accommodationList()) {
                if (p.visitAt() == null) continue;
                LocalDate d = LocalDate.parse(p.visitAt());
                accStart.compute(p.name(), (k, prev) -> (prev == null || d.isBefore(prev)) ? d : prev);
                accEnd.compute(p.name(),   (k, prev) -> (prev == null || d.isAfter(prev))  ? d : prev);
            }
        }

        for (PlanResponseDto.DayDraftDto day : dto.draftDtoList()) {
            if (day.accommodationList() != null) {
                for (PlanResponseDto.PlaceDraftDto placeDraft : day.accommodationList()) {
                    PoiWithDetail poiWDet = searchPoiAndDetail(placeDraft.name());
                    if (poiWDet == null) continue;

                    Place place = findOrCreatePlace(placeDraft.name(), Double.parseDouble(poiWDet.poi.frontLon()), Double.parseDouble(poiWDet.poi.frontLat()), poiWDet.detail.overview(), poiWDet.detail.firstimage(), PlaceType.ACCOMMODATION, placeList);
                    ensureLink(plan, place, createPeriod(accStart.get(placeDraft.name()), accEnd.get(placeDraft.name())), planPlaceList);
                }
            }
            if (day.travelDestList() != null) {
                for (PlanResponseDto.PlaceDraftDto placeDraft : day.travelDestList()) {
                    PoiWithDetail poiWDet = searchPoiAndDetail(placeDraft.name());
                    if (poiWDet == null) continue;

                    Place place = findOrCreatePlace(placeDraft.name(), Double.parseDouble(poiWDet.poi.frontLon()), Double.parseDouble(poiWDet.poi.frontLat()), poiWDet.detail.overview(), poiWDet.detail.firstimage(), PlaceType.TRAVEL_DESTINATION, placeList);
                    ensureLink(plan, place, createPeriod(LocalDate.parse(placeDraft.visitAt()), null), planPlaceList);
                }
            }
            if (day.cafeList() != null) {
                for (PlanResponseDto.PlaceDraftDto placeDraft : day.cafeList()) {
                    PoiWithDetail poiWDet = searchPoiAndDetail(placeDraft.name());
                    if (poiWDet == null) continue;

                    Place place = findOrCreatePlace(placeDraft.name(), Double.parseDouble(poiWDet.poi.frontLon()), Double.parseDouble(poiWDet.poi.frontLat()), poiWDet.detail.overview(), poiWDet.detail.firstimage(), PlaceType.CAFE, placeList);
                    ensureLink(plan, place, createPeriod(LocalDate.parse(placeDraft.visitAt()), null), planPlaceList);
                }
            }
            if (day.restaurantList() != null) {
                for (PlanResponseDto.PlaceDraftDto placeDraft: day.restaurantList()) {
                    PoiWithDetail poiWDet = searchPoiAndDetail(placeDraft.name());
                    if (poiWDet == null) continue;

                    Place place = findOrCreatePlace(placeDraft.name(), Double.parseDouble(poiWDet.poi.frontLon()), Double.parseDouble(poiWDet.poi.frontLat()), poiWDet.detail.overview(), poiWDet.detail.firstimage(), PlaceType.RESTAURANT, placeList);
                    ensureLink(plan, place, createPeriod(LocalDate.parse(placeDraft.visitAt()), null), planPlaceList);
                }
            }
        }

        placeRepository.saveAll(placeList);
        planPlaceRepository.saveAll(planPlaceList);
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

    private Place createPlace(String name, String locX, String locY, String desc, String img, PlaceType type) {
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

    private PoiWithDetail searchPoiAndDetail(String name) {
        List<PoiSearchRes.Poi> pois = tmapPoiSearchClient.searchPois(name, 1);
        if (pois == null || pois.isEmpty()) return null;

        PoiSearchRes.Poi poi = pois.getFirst();
        PlaceResponseDto.TourApiResultDto det = getPlaceDetail(poi);
        return new PoiWithDetail(poi, det);
    }

    private Place findOrCreatePlace(String name, double lon, double lat,
                                    String desc, String img, PlaceType type,
                                    List<Place> placeList) {
        return placeRepository.findByNameAndLocXAndLocYRounded(name, lon, lat)
                .orElseGet(() -> {
                    Place p = createPlace(name, String.valueOf(lon), String.valueOf(lat), desc, img, type);
                    placeList.add(p);
                    return p;
                });
    }

    private void ensureLink(Plan plan, Place place, PlacePeriod period, List<PlanPlace> planPlaceList) {
        if (place == null) return;
        if (!planPlaceRepository.existsByPlanIdAndPlaceId(plan.getId(), place.getId())) {
            PlanPlace link = createPlanPlace(plan, place, period);
            planPlaceList.add(link);
        }
    }

}
