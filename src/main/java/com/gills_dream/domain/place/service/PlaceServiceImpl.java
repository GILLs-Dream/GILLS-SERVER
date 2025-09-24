package com.gills_dream.domain.place.service;

import com.gills_dream.domain.map.dto.MapRouteByDayRes;
import com.gills_dream.domain.map.dto.MapRouteReq;
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
import com.gills_dream.global.apiPayload.code.status.ErrorStatus;
import com.gills_dream.global.apiPayload.exception.GeneralException;
import com.gills_dream.global.map.tourapi.TourApiClient;
import com.gills_dream.global.map.tourapi.TourPlaceDetailRes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.gills_dream.domain.place.enumerate.Platform.TOUR;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceServiceImpl implements PlaceService {
    private final PlaceRepository placeRepository;
    private final MapService mapService;
    private final PlaceCacheService placeCacheService;
    private final TourApiClient tourApiClient;

    @Override
    @Transactional
    public void createList(Plan plan, PlanRequestDto.DestinationDto request) {
        request.tourDtoList().forEach(tourPlaceDto -> {
            TourPlaceDetailRes detail = tourPlaceDto.platform() == TOUR ?
                    tourApiClient.getPlaceDetail(tourPlaceDto.contentId()) : null;

            Place place = placeRepository.findByContentId(tourPlaceDto.contentId())
                    .orElse(Place.builder()
                            .platform(tourPlaceDto.platform())
                            .contentId(tourPlaceDto.contentId())
                            .name(tourPlaceDto.title() != null ? tourPlaceDto.title() : (detail != null ? detail.title() : null))
                            .detailUrl(tourPlaceDto.detailUrl() != null ? tourPlaceDto.detailUrl() : null)
                            .lat(tourPlaceDto.lat() != null ? tourPlaceDto.lat() : (detail != null ? detail.mapy() : null))
                            .lng(tourPlaceDto.lng() != null ? tourPlaceDto.lng() : (detail != null ? detail.mapx() : null))
                            .placeType(PlaceType.TOUR)
                            .images(detail != null ? List.of(detail.firstimage()) : List.of())
                            .build());

            placeRepository.save(place);
            PlanPlace planPlace = PlanPlace.builder()
                    .plan(plan)
                    .place(place)
                    .period(PlacePeriod.builder()
                            .startedAt(tourPlaceDto.date() != null ? LocalDate.parse(tourPlaceDto.date()) : null)
                            .duration(1)
                            .dayOrder(tourPlaceDto.dayOrder() != null ? tourPlaceDto.dayOrder() : null)
                            .build())
                    .build();
            plan.getPlanPlaces().add(planPlace);
            place.getPlanPlaces().add(planPlace);
        });

        request.accommodationDtoList().forEach(accommodationDto -> {
            TourPlaceDetailRes detail = accommodationDto.platform() == TOUR ?
                    tourApiClient.getPlaceDetail(accommodationDto.contentId()) : null;
           Place place = placeRepository.findByContentId(accommodationDto.contentId())
                    .orElse(
                            Place.builder()
                                .platform(accommodationDto.platform())
                                .contentId(accommodationDto.contentId())
                                .name(accommodationDto.title() != null ? accommodationDto.title() : (detail != null ? detail.title() : null))
                                .detailUrl(accommodationDto.detailUrl() != null ? accommodationDto.detailUrl() : null)
                                .lat(accommodationDto.lat() != null ? accommodationDto.lat() : (detail != null ? detail.mapy() : null))
                                .lng(accommodationDto.lng() != null ? accommodationDto.lng() : (detail != null ? detail.mapx() : null))
                                .placeType(PlaceType.ACCOMMODATION)
                                .images(detail != null ? List.of(detail.firstimage()) : List.of())
                                .build());
            placeRepository.save(place);
            PlanPlace planPlace = PlanPlace.builder()
                    .plan(plan)
                    .place(place)
                    .period(
                            PlacePeriod.builder()
                                    .startedAt(accommodationDto.startDate() != null ? LocalDate.parse(accommodationDto.startDate()) : null)
                                    .finishedAt(accommodationDto.endDate() != null ? LocalDate.parse(accommodationDto.endDate()) : null)
                                    .duration(
                                            accommodationDto.startDate() != null && accommodationDto.endDate() != null
                                                    ? Math.toIntExact(
                                                    LocalDate.parse(accommodationDto.endDate()).toEpochDay()
                                                            - LocalDate.parse(accommodationDto.startDate()).toEpochDay()
                                            )
                                                    : null
                                    )
                                    .dayOrder(accommodationDto.dayOrder() != null ? accommodationDto.dayOrder() : null)
                                    .build()
                    )
                    .build();
            plan.getPlanPlaces().add(planPlace);
            place.getPlanPlaces().add(planPlace);
        });

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
    public void updatePlaces(Plan plan, MapRouteByDayRes generated) {
        List<PlanPlace> planPlaceList = plan.getPlanPlaces();
        for (PlanPlace planPlace : planPlaceList) {
            PlacePeriod placePeriod = planPlace.getPeriod();
            MapRouteReq.Location location = generated.days().stream()
                    .flatMap(day -> day.locations().stream())
                    .filter(loc -> loc.name().equals(planPlace.getPlace().getName()))
                    .findFirst().orElseThrow(() -> new GeneralException(ErrorStatus.CANNOT_FIND_PLACE_FROM_DB));
            if (generated.mode() == MapRouteByDayRes.Mode.DATED) {
                if (!placePeriod.getStartedAt().toString().equals(location.period().startedAt()) || !placePeriod.getFinishedAt().toString().equals(location.period().finishedAt())) {
                    placePeriod.updateDate(location.period());
                }
            } else if (generated.mode() == MapRouteByDayRes.Mode.INDEXED) {
                if (!placePeriod.getDayOrder().equals(location.period().dayOrder())) {
                    placePeriod.updateDate(location.period());
                }
            }
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
                                .startedAt(String.valueOf(req.period().startedAt()))
                                .finishedAt(String.valueOf(req.period().finishedAt()))
                                .duration(req.period().duration())
                                .build())
                        .build()
        );
    }
}
