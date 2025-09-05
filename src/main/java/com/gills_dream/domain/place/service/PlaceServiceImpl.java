package com.gills_dream.domain.place.service;

import com.gills_dream.domain.map.dto.MapSearchRes;
import com.gills_dream.domain.map.dto.MapSearchResWithPlatform;
import com.gills_dream.domain.map.service.MapService;
import com.gills_dream.domain.place.dto.PlaceAddedDto;
import com.gills_dream.domain.place.dto.PlaceCacheWithContentId;
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
import com.gills_dream.domain.plan.entity.Plan;
import com.gills_dream.domain.plan.entity.PlanPlace;
import com.gills_dream.domain.plan.repository.PlanPlaceRepository;
import com.gills_dream.global.apiPayload.code.status.ErrorStatus;
import com.gills_dream.global.apiPayload.exception.GeneralException;
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
    private final MapService mapService;
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
            PlaceCache placeCache = placeCacheService.getOne(plan.getId(), tourDto.contentId());
            if (placeCache == null) throw new GeneralException(ErrorStatus.CANNOT_FIND_PLACE_FROM_REDIS);

            // DB에 저장된 장소인지 확인 & 없으면 DB에 저장
            Place place = placeRepository.findByNameAndLatAndLngRounded(placeCache.name(), Double.parseDouble(placeCache.lat()), Double.parseDouble(placeCache.lng()))
                    .orElseGet(() -> {
                            Place newPlace = Place.builder()
                                    .name(placeCache.name())
                                    .lat(placeCache.lat())
                                    .lng(placeCache.lng())
                                    .detailUrl(placeCache.detailUrl())
                                    .contentId(tourDto.contentId())
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
                        .startedAt(LocalDate.parse(tourDto.date()))
                        .finishedAt(LocalDate.parse(tourDto.date()))
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
            PlaceCache placeCache = placeCacheService.getOne(plan.getId(), accomDto.contentId());
            if (placeCache == null) throw new GeneralException(ErrorStatus.CANNOT_FIND_PLACE_FROM_REDIS);

            // DB에 저장된 장소인지 확인 & 없으면 DB에 저장
            Place place = placeRepository.findByNameAndLatAndLngRounded(placeCache.name(), Double.parseDouble(placeCache.lat()), Double.parseDouble(placeCache.lng()))
                    .orElseGet(() -> {
                                Place newPlace = Place.builder()
                                        .name(placeCache.name())
                                        .lat(placeCache.lat())
                                        .lng(placeCache.lng())
                                        .detailUrl(placeCache.detailUrl())
                                        .contentId(accomDto.contentId())
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
                Integer duration = Math.toIntExact((accomDto.startDate() != null && accomDto.endDate() != null) ? ChronoUnit.DAYS.between(LocalDate.parse(accomDto.startDate()), LocalDate.parse(accomDto.endDate())) : 0);
                PlacePeriod period = PlacePeriod.builder()
                        .startedAt(LocalDate.parse(accomDto.startDate()))
                        .finishedAt(LocalDate.parse(accomDto.endDate()))
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
    public List<PlaceCacheWithContentId> searchPlaces(Plan plan, PlacePair placePair) {
        List<PlaceCacheWithContentId> placeCacheList = new ArrayList<>();

        placeCacheList.addAll(processPlaceList(placePair.tourList(), PlaceType.TOUR));
        placeCacheList.addAll(processPlaceList(placePair.accommodationList(), PlaceType.ACCOMMODATION));

        return placeCacheList;
    }

    private List<PlaceCacheWithContentId> processPlaceList(List<String> names, PlaceType placeType) {
        List<PlaceCacheWithContentId> result = new ArrayList<>();

        for (String name : names) {
            MapSearchResWithPlatform search = mapService.searchByKeyword(name, 1, 10);

            if (search.mapSearchResList().isEmpty()) continue;
            MapSearchRes target = search.mapSearchResList().stream()
                    .filter(res -> res.name().equals(name))
                    .findFirst()
                    .orElse(null);
            if (target == null) continue;

            PlaceCache newPlaceCache = createPlaceCache(search.platform(), name, target, placeType);
            result.add(PlaceCacheWithContentId.builder()
                    .contentId(target.id())
                    .placeCache(newPlaceCache)
                    .build());
        }
        return result;
    }

    private PlaceCache createPlaceCache(Platform platform, String name, MapSearchRes target, PlaceType placeType) {
        return PlaceCache.builder()
                .platform(String.valueOf(platform))
                .name(name)
                .placeType(String.valueOf(placeType))
                .lat(target.lat())
                .lng(target.lng())
                .imageUrl(target.imageUrl())
                .detailUrl(target.detailUrl())
                .build();
    }

    @Override
    public List<PlaceAddedDto> addToRedis(Plan plan, List<PlaceCacheWithContentId> placeCacheList) {
        List<PlaceAddedDto> placeAddedList = new ArrayList<>();
        for (PlaceCacheWithContentId placeCacheDto : placeCacheList) {
            placeCacheService.save(placeCacheDto.placeCache().platform(), placeCacheDto.contentId(), plan.getId(), placeCacheDto.placeCache());
            placeAddedList.add(PlaceAddedDto.builder()
                    .contentId(placeCacheDto.contentId())
                    .name(placeCacheDto.placeCache().name()).build());
        }

        return placeAddedList;
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
                                .startedAt(String.valueOf(req.period().startedAt()))
                                .finishedAt(String.valueOf(req.period().finishedAt()))
                                .duration(req.period().duration())
                                .build())
                        .build()
        );
    }
}
