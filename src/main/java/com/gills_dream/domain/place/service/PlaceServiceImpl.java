package com.gills_dream.domain.place.service;

import com.gills_dream.domain.place.dto.PlaceResponseDto;
import com.gills_dream.domain.place.dto.PlaceSearchReq;
import com.gills_dream.domain.place.dto.PlaceSearchRes;
import com.gills_dream.domain.place.entity.Place;
import com.gills_dream.domain.place.enumerate.PlaceType;
import com.gills_dream.domain.place.enumerate.Platform;
import com.gills_dream.domain.place.record.PlacePair;
import com.gills_dream.domain.place.repository.PlaceRepository;
import com.gills_dream.domain.place.vo.PlaceCache;
import com.gills_dream.domain.place.vo.PlacePeriod;
import com.gills_dream.domain.plan.dto.PlanRequestDto;
import com.gills_dream.domain.plan.dto.PlanResponseDto;
import com.gills_dream.domain.plan.entity.Plan;
import com.gills_dream.domain.plan.entity.PlanPlace;
import com.gills_dream.domain.plan.repository.PlanPlaceRepository;
import com.gills_dream.domain.plan.repository.PlanRepository;
import com.gills_dream.global.apiPayload.code.status.ErrorStatus;
import com.gills_dream.global.apiPayload.exception.GeneralException;
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
    private final PlanRepository planRepository;

    private record PoiWithDetail(PoiSearchRes.Poi poi, PlaceResponseDto.TourApiResultDto detail) {}

    private final PlaceCacheService placeCacheService;

    @Override
    @Transactional
    public void createList(Plan plan, PlanRequestDto.DestinationDto request) {
        List<PlanRequestDto.TourPlaceDto> tourDtoList = request.tourDtoList();
        List<PlanRequestDto.AccommodationDto> accomDtoList = request.accommodationDtoList();

        List<Place> newPlaceList = new ArrayList<>();
        List<PlanPlace> newPlanPlaceList = new ArrayList<>();

        for (PlanRequestDto.TourPlaceDto tourDto : tourDtoList) {
            // Redis에서 해당 장소 불러오기
            PlaceCache placeCache = placeCacheService.getOne(plan.getId(), tourDto.placeCode());
            if (placeCache == null) throw new GeneralException(ErrorStatus.CANNOT_FIND_PLACE_FROM_REDIS);

            // DB에 저장된 장소인지 확인 & 없으면 DB에 저장
            Place place = placeRepository.findByNameAndLatAndLngRounded(placeCache.name(), Double.parseDouble(placeCache.lat()), Double.parseDouble(placeCache.lng()))
                    .orElseGet(() -> {
                            Place newPlace = Place.builder()
                                    .name(placeCache.name())
                                    .lat(placeCache.lat())
                                    .lng(placeCache.lng())
                                    .detailUrl(placeCache.detailUrl())
                                    .code(tourDto.placeCode())
                                    .platform(Platform.valueOf(placeCache.platform()))
                                    .placeType(PlaceType.TOUR)
                                    .build();
                            newPlace.addImage(placeCache.imageUrl());
                            newPlaceList.add(newPlace);
                            return newPlace;
                        }
                    );

            // PlanPlace 생성 & Period 저장
            if (!planPlaceRepository.existsByPlanIdAndPlaceId(plan.getId(), place.getId())) {
                PlacePeriod period = PlacePeriod.builder()
                        .startedAt(tourDto.date())
                        .finishedAt(tourDto.date())
                        .duration(1)
                        .build();

                PlanPlace newPlanPlace = PlanPlace.builder()
                        .plan(plan)
                        .place(place)
                        .period(period)
                        .build();

                newPlanPlaceList.add(newPlanPlace);
                place.getPlanPlaces().add(newPlanPlace);
            }
        }

        for (PlanRequestDto.AccommodationDto accomDto : accomDtoList) {
            // Redis에서 해당 장소 불러오기
            PlaceCache placeCache = placeCacheService.getOne(plan.getId(), accomDto.placeCode());
            if (placeCache == null) throw new GeneralException(ErrorStatus.CANNOT_FIND_PLACE_FROM_REDIS);

            // DB에 저장된 장소인지 확인 & 없으면 DB에 저장
            Place place = placeRepository.findByNameAndLatAndLngRounded(placeCache.name(), Double.parseDouble(placeCache.lat()), Double.parseDouble(placeCache.lng()))
                    .orElseGet(() -> {
                                Place newPlace = Place.builder()
                                        .name(placeCache.name())
                                        .lat(placeCache.lat())
                                        .lng(placeCache.lng())
                                        .detailUrl(placeCache.detailUrl())
                                        .code(accomDto.placeCode())
                                        .platform(Platform.valueOf(placeCache.platform()))
                                        .placeType(PlaceType.ACCOMMODATION)
                                        .build();
                                newPlace.addImage(placeCache.imageUrl());
                                newPlaceList.add(newPlace);
                                return newPlace;
                            }
                    );

            // PlanPlace 생성 & Period 저장
            if (!planPlaceRepository.existsByPlanIdAndPlaceId(plan.getId(), place.getId())) {
                Integer duration = Math.toIntExact((accomDto.startDate() != null && accomDto.endDate() != null) ? ChronoUnit.DAYS.between(accomDto.startDate(), accomDto.endDate()) : 0);
                PlacePeriod period = PlacePeriod.builder()
                        .startedAt(accomDto.startDate())
                        .finishedAt(accomDto.endDate())
                        .duration(duration)
                        .build();

                PlanPlace newPlanPlace = PlanPlace.builder()
                        .plan(plan)
                        .place(place)
                        .period(period)
                        .build();

                newPlanPlaceList.add(newPlanPlace);
                place.getPlanPlaces().add(newPlanPlace);
            }
        }

        if (!newPlaceList.isEmpty()) placeRepository.saveAll(newPlaceList);
        if (!newPlanPlaceList.isEmpty()) planPlaceRepository.saveAll(newPlanPlaceList);

        plan.getPlanPlaces().addAll(newPlanPlaceList);
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

//    @Override
//    public void addToRedis(ContentType contentType, Plan plan, PlacePair placePair) {
//    }

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
