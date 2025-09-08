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

    /** 지오펜스 반경(km) – 지역 밖/이상치 필터 기준 */
    private static final double KM_GEOFENCE = 30.0;

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

        log.info("placeList : {}", placeList);

        // 캐시 병합(없으면 무시)
        List<PlaceCache> cachePlaces = placeCacheService.getByPlanId(planId);
        log.info("cachePlaces : {}", cachePlaces);

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

        log.info("cached placeList : {}", placeList);

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

        log.info("locations : {}", locations);

        MapRouteReq route = MapRouteReq.of(plan.getRegion(), plan.getPeriod(), locations);
        log.info("route : {}", route);

        long count = route.locations().stream()
                .filter(l -> l.lat() != 0 && l.lng() != 0)
                .count();

        // 3) GPT로 날짜/인덱스별 재정렬 결과
        MapRouteByDayRes byDay = gptService.createRouteRequest(
                route, transportation.toString(), count
        );

        log.info("byDay (raw) : {}", byDay);

        // ===== [A] DATED 모드: day.date로 period.startedAt/finishedAt 강제 정규화 =====
        if ("DATED".equalsIgnoreCase(String.valueOf(byDay.mode()))) {
            for (var d : byDay.days()) {
                if (d.locations() == null) continue;
                String dayDate = d.date(); // "YYYY-MM-DD"
                for (int i = 0; i < d.locations().size(); i++) {
                    var loc = d.locations().get(i);
                    var p = loc.period();
                    MapRouteReq.Period np = MapRouteReq.Period.of(
                            (p == null || p.startedAt() == null || p.startedAt().isBlank()) ? dayDate : dayDate,
                            (p != null && p.finishedAt() != null && !p.finishedAt().isBlank()) ? dayDate : dayDate,
                            (p != null) ? p.duration() : null,
                            (p != null && p.dayOrder() != null) ? p.dayOrder() : d.index()
                    );
                    d.locations().set(i, MapRouteReq.Location.of(
                            loc.lat(), loc.lng(), loc.name(), loc.placeType(), np,
                            loc.contentId(), loc.detailUrl(), loc.imageUrl(), loc.platform()
                    ));
                }
            }
        }

        // ===== [B] contentId 비어있을 경우 카카오 검색으로 보강하되, 지역/거리 기반 후보만 허용 =====
        byDay.days().forEach(d -> {
            if (d.locations() == null || d.locations().isEmpty()) return;

            double[] ctr = centroid(d.locations());
            String regionName = plan.getRegion() == null ? "" : plan.getRegion().trim();

            for (int i = 0; i < d.locations().size(); i++) {
                var loc = d.locations().get(i);
                boolean needSearch = (loc.contentId() == null || loc.contentId().isBlank());
                if (!needSearch || loc.name() == null || loc.name().isBlank()) continue;

                List<MapSearchRes> results = kakaoMapClient.searchByKeyword(loc.name());
                if (results == null) results = List.of();

                List<MapSearchRes> candidates = results.stream()
                        .filter(r -> {
                            double rLat = parseDoubleSafe(r.lat());
                            double rLng = parseDoubleSafe(r.lng());
                            if (rLat == 0 || rLng == 0) return false;
                            if (!inGeofence(ctr[0], ctr[1], rLat, rLng)) return false;
                            if (!nz(regionName).isEmpty()) {
                                String nm = nz(r.name()).toLowerCase(Locale.ROOT);
                                String rn = regionName.toLowerCase(Locale.ROOT);
                            }
                            return true;
                        })
                        .sorted(Comparator.comparingDouble(r ->
                                haversineKm(loc.lat(), loc.lng(), parseDoubleSafe(r.lat()), parseDoubleSafe(r.lng()))
                        ))
                        .toList();

                if (!candidates.isEmpty()) {
                    MapSearchRes best = candidates.get(0);
                    MapRouteReq.Location newLoc = MapRouteReq.Location.updateContent(
                            loc,
                            best.id(), best.detailUrl(), best.imageUrl(), "KAKAO",
                            parseDoubleSafe(best.lat()), parseDoubleSafe(best.lng()), best.name()
                    );
                    d.locations().set(i, newLoc);
                } else {
                    String genId = genContentId();
                    MapRouteReq.Location newLoc = MapRouteReq.Location.updateContent(
                            loc,
                            genId,            // contentId
                            null,             // detailUrl
                            null,             // imageUrl
                            "GENERATED",      // platform
                            loc.lat(),        // 좌표
                            loc.lng(),
                            loc.name()
                    );
                    d.locations().set(i, newLoc);
                    log.warn("contentId not found via Kakao. Generated contentId={} for name='{}'", genId, nz(loc.name()));
                }
            }
        });

        // 4) Provider 실행기 선택
        Function<MapRouteReq, MapRouteRes> runner = switch (transportation) {
            case CAR     -> carRouteProvider::route;
            case WALK    -> walkRouteProvider::route;
            case BICYCLE -> bicycleRouteProvider::route;
            case TRANSPORT -> transitRouteProvider::route;
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
                    // ===== [C] 이상거리(스파이크) 및 지역 밖 leg 제거 =====
                    double[] ctr = centroid(day.locations());
                    var filteredLegs = dayRes.legs().stream()
                            .filter(l -> {
                                // 단일 leg 길이 제한 (지오펜스 * 1,000 m)
                                // 지오펜스 검사: 시작/끝 모두 day centroid 반경 내
                                boolean okStart = inGeofence(ctr[0], ctr[1], l.startLat(), l.startLng());
                                boolean okEnd   = inGeofence(ctr[0], ctr[1], l.endLat(),   l.endLng());
                                return okStart && okEnd;
                            })
                            .toList();

                    if (!filteredLegs.isEmpty()) {
                        dayRes = MapRouteRes.builder()
                                .distance(dayRes.distance())
                                .duration(dayRes.duration())
                                .polyline(dayRes.polyline())
                                .legs(filteredLegs)
                                .build();

                        // ★ PlaceInfo 보강(입력 locations로 역매핑)
                        List<MapRouteRes> enrichedRoutes = enrichRoutesWithPlaceInfo(List.of(dayRes), day.locations());
                        routes.addAll(enrichedRoutes);
                    } else {
                        log.warn("day {}/{}: all legs removed by spike/geofence filter",
                                nz(day.date()), nz(String.valueOf(day.index())));
                    }
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

    private String genContentId() {
        return "GEN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    /**
     * MapRouteRes의 각 Leg에 PlaceInfo가 없으면 locs로부터 역매핑하여 보강
     * @param routes
     * @param locs
     * @return
     */
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
                        .modes(l.modes())
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

    /**
     * Location 목록으로부터 key(좌표, 이름) → PlaceInfo 인덱스 생성
     * @param locs
     * @return
     */
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

        // 1) 메모리 캐시 우선
        Place cached = placeByContentId.get(contentId);
        if (cached != null) return cached;

        // 2) DB 조회
        Optional<Place> found = placeRepository.findByContentId(contentId);
        if (found.isPresent()) {
            placeByContentId.put(contentId, found.get());
            return found.get();
        }

        // 3) 신규 저장
        Place newPlace = Place.builder()
                .contentId(contentId)
                .name(name)
                .lat(String.valueOf(lat))
                .lng(String.valueOf(lng))
                .placeType(parsePlaceType(placeTypeStr))
                .detailUrl(detailUrl)
                .images(imageUrl != null ? List.of(imageUrl) : Collections.emptyList())
                .build();
        placeRepository.save(newPlace);
        placeByContentId.put(contentId, newPlace);
        return newPlace;
    }

    private PlaceType parsePlaceType(String s) {
        if (s == null || s.isBlank() || s.equalsIgnoreCase("NULL")) {
            return PlaceType.TOUR; // 기본값
        }
        try {
            return PlaceType.valueOf(s.trim().toUpperCase(Locale.ROOT));
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

    /**
     * 안전한 double 파싱 (null/예외 시 0.0 반환)
     * @param s
     * @return
     */
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


    /**
     * haversine 공식에 의한 두 지점 간 거리 계산 (km)
     * @param lat1
     * @param lng1
     * @param lat2
     * @param lng2
     * @return
     */
    private double haversineKm(double lat1, double lng1, double lat2, double lng2) {
        final double R = 6371.0088;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat/2)*Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2))*
                        Math.sin(dLng/2)*Math.sin(dLng/2);
        return 2 * R * Math.asin(Math.sqrt(a));
    }

    /**
     * 위치들의 중심 좌표 계산 (lat/lng 평균)
     * @param locs
     * @return
     */
    private double[] centroid(List<MapRouteReq.Location> locs) {
        double lat = 0, lng = 0; int n = 0;
        for (var l : locs) {
            if (l.lat() != 0 && l.lng() != 0) { lat += l.lat(); lng += l.lng(); n++; }
        }
        return (n == 0) ? new double[]{0, 0} : new double[]{lat / n, lng / n};
    }

    /**
     * 기준점(baseLat, baseLng)으로부터 대상점(lat, lng)이 지오펜스 내에 있는지 여부
     * @param baseLat
     * @param baseLng
     * @param lat
     * @param lng
     * @return
     */
    private boolean inGeofence(double baseLat, double baseLng, double lat, double lng) {
        return haversineKm(baseLat, baseLng, lat, lng) <= KM_GEOFENCE;
    }
}