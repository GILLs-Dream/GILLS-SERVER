package com.gills_dream.domain.map.service;

import com.gills_dream.domain.gpt.service.GptService;
import com.gills_dream.domain.map.dto.MapRouteByDayRes;
import com.gills_dream.domain.map.dto.MapRouteReq;
import com.gills_dream.domain.map.dto.MapRouteRes;
import com.gills_dream.domain.map.dto.MapSearchRes;
import com.gills_dream.domain.place.entity.Place;
import com.gills_dream.domain.place.enumerate.PlaceType;
import com.gills_dream.domain.place.repository.PlaceRepository;
import com.gills_dream.domain.place.service.PlaceCacheService;
import com.gills_dream.domain.place.vo.PlaceCache;
import com.gills_dream.domain.place.vo.PlacePeriod;
import com.gills_dream.domain.plan.entity.Plan;
import com.gills_dream.domain.plan.entity.PlanPlace;
import com.gills_dream.domain.plan.enumerate.TransportType;
import com.gills_dream.domain.plan.repository.PlanRepository;
import com.gills_dream.global.apiPayload.code.status.ErrorStatus;
import com.gills_dream.global.apiPayload.exception.GeneralException;
import com.gills_dream.global.map.kakao.KakaoMapClient;
import com.gills_dream.global.map.provider.BicycleRouteProvider;
import com.gills_dream.global.map.provider.CarRouteProvider;
import com.gills_dream.global.map.provider.TransitRouteProvider;
import com.gills_dream.global.map.provider.WalkRouteProvider;
import com.gills_dream.global.map.util.RouteUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouteService {

    private final CarRouteProvider carRouteProvider;
    private final WalkRouteProvider walkRouteProvider;
    private final BicycleRouteProvider bicycleRouteProvider;
    private final TransitRouteProvider transitRouteProvider;
    private final PlaceCacheService placeCacheService;
    private final GptService gptService;
    private final PlaceRepository placeRepository;
    private final PlanRepository planRepository;
    private final KakaoMapClient kakaoMapClient;

    /**
     * 경로 조회 (GPT가 만든 날짜/인덱스별 locations로 provider 호출 → routes만 채워서 반환)
     * - Provider가 PlaceInfo를 세팅하지 않아도, 본 서비스에서 day.locations로 역매핑하여 PlaceInfo 보강
     */
    @Transactional
    public MapRouteByDayRes getRoute(Long planId, TransportType transportation) throws GeneralException {
        // 1) plan + places(planPlaces까지 fetch)
        Plan plan = planRepository.findById(Math.toIntExact(planId))
                .orElseThrow(() -> new GeneralException(ErrorStatus.PLAN_NOT_FOUND));

        // 패치 조인으로 planPlaces(+plan) 미리 로딩되는 메서드 사용 권장
        // findAllByPlanId는 planPlaces를 fetch-join, findAllWithImagesByIdIn는 images 초기화용
        List<Place> places = placeRepository.findAllByPlanId(planId);
        if (!places.isEmpty()) {
            var ids = places.stream().map(Place::getId).toList();
            placeRepository.findAllWithImagesByIdIn(ids);
        } else {
            places = new ArrayList<>();
        }

        // Place → PlaceCache 변환 (해당 planId의 PlanPlace.period 사용)
        List<PlaceCache> placeList = new ArrayList<>(places.size());
        for (Place p : places) {
            PlacePeriod period = p.getPlanPlaces().stream()
                    .filter(pp -> pp.getPlan() != null && planId.equals(pp.getPlan().getId()))
                    .map(PlanPlace::getPeriod)
                    .findFirst()
                    .orElse(null);
            placeList.add(PlaceCache.fromEntity(p, period));
        }

        log.info("placeList : " + placeList);

        // 캐시 병합(없으면 무시)
        List<PlaceCache> cachePlaces = placeCacheService.getByPlanId(planId);
        log.info("cachePlaces"  + cachePlaces);

        if (cachePlaces != null && !cachePlaces.isEmpty()) {
            // key = platform:contentId 로 중복 방지
            Set<String> keys = placeList.stream()
                    .map(pc -> (nz(pc.platform()) + ":" + nz(pc.contentId())))
                    .collect(Collectors.toSet());
            for (PlaceCache c : cachePlaces) {
                String key = nz(c.platform()) + ":" + nz(c.contentId());
                if (!keys.contains(key)) {
                    placeList.add(c);
                    keys.add(key);
                }
            }
        }

        if (placeList.size() < 2) {
            throw new GeneralException(ErrorStatus.INVALID_ROUTE_REQUEST);
        }

        log.info("cached placeList : " + placeList);

        // 2) PlaceCache → MapRouteReq.Location 변환
        List<MapRouteReq.Location> locations = placeList.stream()
                .map(p -> MapRouteReq.Location.of(
                        parseDoubleSafe(p.lat()),
                        parseDoubleSafe(p.lng()),
                        p.name(),
                        p.placeType() != null ? p.placeType() : null,
                        p.period() == null ? null :
                                MapRouteReq.Period.of(
                                        p.period().startedAt(),
                                        p.period().finishedAt(),
                                        p.period().duration(),
                                        p.period().dayOrder()
                                ),
                        p.contentId(),
                        p.detailUrl(),
                        p.imageUrl(),
                        p.platform()
                ))
                .toList();

        log.info("locations : " + locations);

        MapRouteReq route = MapRouteReq.of(plan.getRegion(), plan.getPeriod(), locations);
        log.info("route : " + route);
        long count = route.locations().stream()
                .filter(l -> l.lat() != 0 && l.lng() != 0)
                .count();

        // 3) GPT로 날짜/인덱스별 재정렬 결과
        MapRouteByDayRes byDay = gptService.createRouteRequest(
                route, TransportType.toCategory(transportation), count
        );

        log.info("byDay : " + byDay);

        // locations를 돌며 contentId가 비어있는 경우, kakaoMapClient.searchByKeyword(keyword)로 정보 받아서 넣기
        byDay.days().forEach(d -> {
            if (d.locations() != null) {
                for (int i = 0; i < d.locations().size(); i++) {
                    var loc = d.locations().get(i);
                    if ((loc.contentId() == null || loc.contentId().isBlank())
                            && loc.name() != null && !loc.name().isBlank()
                            && loc.lat() != 0 && loc.lng() != 0) {
                        List<MapSearchRes> searchResults = kakaoMapClient.searchByKeyword(loc.name());
                        if (searchResults != null && !searchResults.isEmpty()) {
                            // 가장 첫번째 결과 사용
                            MapSearchRes found = searchResults.get(0);
                            MapRouteReq.Location newLoc = MapRouteReq.Location.updateContent(loc, found.id(), found.detailUrl(), found.imageUrl(), "KAKAO",
                                    parseDoubleSafe(found.lat()), parseDoubleSafe(found.lng()), found.name());
                            d.locations().set(i, newLoc); // 변경된 Location으로 교체
                        }
                    }
                }
            }
        });

        // 4) Provider 실행기 선택
        Function<MapRouteReq, MapRouteRes> runner = switch (transportation) {
            case CAR     -> carRouteProvider::route;
            case WALK    -> walkRouteProvider::route;
            case BICYCLE -> bicycleRouteProvider::route;
            case TRANSPORT -> transitRouteProvider::route; // 대중교통
            default -> throw new GeneralException(ErrorStatus.TRANSPORT_TYPE_NOT_FOUND);
        };

        // 5) 날짜별 Provider 호출 + routes 반영(+ PlaceInfo 보강)
        List<MapRouteByDayRes.Day> enrichedDays = new ArrayList<>();

        // contentId → Place 엔티티 캐시 (PlanPlace 연결 시 중복 저장 방지)
        Map<String, Place> placeByContentId = new HashMap<>();

        for (var day : byDay.days()) {
            List<MapRouteRes> routes = new ArrayList<>();

            if (day.locations() != null && day.locations().size() >= 2) {
                MapRouteReq oneDayReq = MapRouteReq.of(plan.getRegion(), plan.getPeriod(), day.locations());
                MapRouteRes dayRes = runner.apply(oneDayReq);

                if (!RouteUtils.isEmpty(dayRes)) {
                    // ★ PlaceInfo 보강(입력 locations로 역매핑)
                    List<MapRouteRes> enrichedRoutes = enrichRoutesWithPlaceInfo(List.of(dayRes), day.locations());
                    routes.addAll(enrichedRoutes);
                }
            }

            log.info("day {} routes: {}", nz(day.date()) + "/" + nz(String.valueOf(day.index())), routes);

            // (선택) DB 반영: START/END Place/PlanPlace 연결
            for (MapRouteRes r : routes) {
                for (var leg : r.legs()) {
                    // START
                    if (leg.startPlace() != null && leg.startPlace().contentId() != null) {
                        var sp = leg.startPlace();
                        Place startPlace = upsertPlaceEntity(
                                placeByContentId,
                                sp.contentId(),
                                leg.startName(),
                                leg.startLat(),
                                leg.startLng(),
                                safeType(sp.placeType()),
                                sp.detailUrl(),
                                sp.imageUrl()
                        );
                        upsertPlanPlacePeriod(plan, startPlace, sp.period());
                    }
                    // END
                    if (leg.endPlace() != null && leg.endPlace().contentId() != null) {
                        var ep = leg.endPlace();
                        Place endPlace = upsertPlaceEntity(
                                placeByContentId,
                                ep.contentId(),
                                leg.endName(),
                                leg.endLat(),
                                leg.endLng(),
                                safeType(ep.placeType()),
                                ep.detailUrl(),
                                ep.imageUrl()
                        );
                        upsertPlanPlacePeriod(plan, endPlace, ep.period());
                    }
                }
            }

            enrichedDays.add(MapRouteByDayRes.Day.builder()
                    .date(day.date())
                    .index(day.index())
                    .locations(day.locations())
                    .routes(routes)
                    .build());
        }

        return MapRouteByDayRes.builder()
                .mode(byDay.mode())
                .days(enrichedDays)
                .build();
    }

    private List<MapRouteRes> enrichRoutesWithPlaceInfo(List<MapRouteRes> routes, List<MapRouteReq.Location> locs) {
        Map<String, MapRouteRes.Leg.PlaceInfo> idx = buildIndex(locs);
        List<MapRouteRes> out = new ArrayList<>(routes.size());
        for (var r : routes) {
            List<MapRouteRes.Leg> newLegs = new ArrayList<>(r.legs().size());
            for (var l : r.legs()) {
                MapRouteRes.Leg.PlaceInfo sp = l.startPlace();
                if (sp == null) {
                    sp = Optional.ofNullable(idx.get(key(l.startLat(), l.startLng())))
                            .orElse(idx.get(norm(l.startName())));
                }
                MapRouteRes.Leg.PlaceInfo ep = l.endPlace();
                if (ep == null) {
                    ep = Optional.ofNullable(idx.get(key(l.endLat(), l.endLng())))
                            .orElse(idx.get(norm(l.endName())));
                }
                newLegs.add(MapRouteRes.Leg.builder()
                        .startName(l.startName())
                        .startLat(l.startLat())
                        .startLng(l.startLng())
                        .startPlace(sp)
                        .endName(l.endName())
                        .endLat(l.endLat())
                        .endLng(l.endLng())
                        .endPlace(ep)
                        .mode(l.mode())
                        .distance(l.distance())
                        .duration(l.duration())
                        .build());
            }
            out.add(MapRouteRes.builder()
                    .distance(r.distance())
                    .duration(r.duration())
                    .polyline(r.polyline())
                    .legs(newLegs)
                    .build());
        }
        return out;
    }

    private Map<String, MapRouteRes.Leg.PlaceInfo> buildIndex(List<MapRouteReq.Location> locs) {
        Map<String, MapRouteRes.Leg.PlaceInfo> idx = new HashMap<>();
        for (var l : locs) {
            MapRouteRes.Leg.PlaceInfo pi = new MapRouteRes.Leg.PlaceInfo(
                    l.platform(),
                    l.contentId(),
                    l.placeType(),
                    l.detailUrl(),
                    l.imageUrl(),
                    l.period() == null ? null :
                            new MapRouteRes.Leg.PlaceInfo.Period(
                                    l.period().startedAt(),
                                    l.period().finishedAt(),
                                    l.period().duration(),
                                    l.period().dayOrder()
                            )
            );
            idx.put(key(l.lat(), l.lng()), pi);
            if (l.name() != null && !l.name().isBlank()) {
                idx.putIfAbsent(norm(l.name()), pi);
            }
        }
        return idx;
    }

    private String key(double lat, double lng) {
        return String.format(Locale.ROOT, "%.6f,%.6f", lat, lng);
    }

    private String norm(String s) {
        return (s == null) ? "" : s.trim().toLowerCase(Locale.ROOT);
    }

    /** contentId 기준 Place 캐시/저장 (없으면 생성 저장, 있으면 재사용) */
    private Place upsertPlaceEntity(
            Map<String, Place> placeByContentId,
            String contentId,
            String name,
            double lat,
            double lng,
            String placeTypeStr,
            String detailUrl,
            String imageUrl
    ) {
        if (contentId == null || contentId.isBlank()) return null;
        return placeByContentId.computeIfAbsent(contentId, cid -> {
            Place newPlace = Place.builder()
                    .contentId(cid)
                    .name(name)
                    .lat(String.valueOf(lat))
                    .lng(String.valueOf(lng))
                    .placeType(parsePlaceType(placeTypeStr))
                    .detailUrl(detailUrl)
                    .images(imageUrl != null ? List.of(imageUrl) : Collections.emptyList())
                    .build();
            placeRepository.save(newPlace);
            return newPlace;
        });
    }

    private PlaceType parsePlaceType(String s) {
        if (s == null || s.isBlank() || s.equalsIgnoreCase("NULL")) {
            return PlaceType.TOUR; // 기본값
        }
        try {
            return PlaceType.valueOf(s.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Unknown placeType: {}, defaulting to TOUR", s);
            return PlaceType.TOUR;
        }
    }

    /**
     * (plan, place) 조합의 PlanPlace가 존재하면 period만 갱신(merge),
     * 없으면 새 PlanPlace 생성하여 연결.
     */
    private void upsertPlanPlacePeriod(Plan plan, Place place, MapRouteRes.Leg.PlaceInfo.Period p) {
        if (place == null) return;

        // 기존 PlanPlace 탐색
        PlanPlace existing = null;
        for (PlanPlace pp : plan.getPlanPlaces()) {
            if (pp.getPlace() != null && pp.getPlace().getId() != null
                    && place.getId() != null
                    && pp.getPlace().getId().equals(place.getId())) {
                existing = pp;
                break;
            }
        }

        // Period 변환(null-safe)
        LocalDate startedAt  = (p == null || p.startedAt() == null || p.startedAt().isBlank())
                ? null : LocalDate.parse(p.startedAt());
        LocalDate finishedAt = (p == null || p.finishedAt() == null || p.finishedAt().isBlank())
                ? null : LocalDate.parse(p.finishedAt());
        Integer duration     = (p == null) ? null : p.duration();
        Integer dayOrder     = (p == null) ? null : p.dayOrder();

        if (existing == null) {
            // 새 PlanPlace 생성
            PlacePeriod newPeriod = PlacePeriod.builder()
                    .startedAt(startedAt)
                    .finishedAt(finishedAt)
                    .duration(duration)
                    .dayOrder(dayOrder)
                    .build();

            PlanPlace planPlace = PlanPlace.builder()
                    .plan(plan)
                    .place(place)
                    .period(newPeriod)
                    .build();

            plan.getPlanPlaces().add(planPlace);
            place.getPlanPlaces().add(planPlace);
        } else {
            // 기존 PlanPlace의 period만 갱신(merge: null 아닌 값만 덮어쓰기)
            PlacePeriod cur = existing.getPeriod();
            if (cur == null) {
                cur = PlacePeriod.builder().build();
                existing.setPeriod(cur);
            }
            if (startedAt  != null) cur.setStartedAt(startedAt);
            if (finishedAt != null) cur.setFinishedAt(finishedAt);
            if (duration   != null) cur.setDuration(duration);
            if (dayOrder   != null) cur.setDayOrder(dayOrder);
        }
    }

    private double parseDoubleSafe(String s) {
        try { return s == null ? 0.0 : Double.parseDouble(s); }
        catch (Exception e) { return 0.0; }
    }

    private String safeType(String s) {
        return (s == null) ? PlaceType.TOUR.name() : s.trim().toUpperCase(Locale.ROOT);
    }

    private String nz(String s) {
        return (s == null) ? "" : s;
    }
}