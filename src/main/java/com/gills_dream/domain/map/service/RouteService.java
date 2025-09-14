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

    // ===== 공통 유틸 =====
    private static <T> List<T> nlist(Collection<T> c) { return (c == null) ? Collections.emptyList() : new ArrayList<>(c); }
    private static <T> Set<T> nset(Collection<T> c)    { return (c == null) ? Collections.emptySet() : new HashSet<>(c); }
    private static boolean isBlank(String s)           { return s == null || s.isBlank(); }
    private String nz(String s)                        { return (s == null) ? "" : s; }

    /**
     * 경로 조회 (GPT가 만든 날짜/인덱스별 locations로 provider 호출 → routes만 채워서 반환)
     * - Provider가 PlaceInfo를 세팅하지 않아도, 본 서비스에서 day.locations로 역매핑하여 PlaceInfo 보강
     */
    @Transactional
    public MapRouteByDayRes getRoute(Long planId, TransportType transportation) throws GeneralException {
        try {
            // 1) plan + categories
            Plan plan = planRepository.findByIdWithCategories(planId)
                    .orElseThrow(() -> new GeneralException(ErrorStatus.PLAN_NOT_FOUND));

            // plan category List<String>
            List<String> categories = nset(plan.getCategories()).stream()
                    .filter(Objects::nonNull)
                    .map(c -> c.getDescription())
                    .filter(s -> !isBlank(s))
                    .distinct()
                    .toList();

            // 2) places (PlanPlace, images 사전 로딩)
            List<Place> places = nlist(placeRepository.findAllByPlanId(planId));
            if (!places.isEmpty()) {
                var ids = places.stream().map(Place::getId).filter(Objects::nonNull).toList();
                if (!ids.isEmpty()) placeRepository.findAllWithImagesByIdIn(ids);
            }

            // Place -> PlaceCache (PlanPlace.period 매핑, 널-세이프)
            List<PlaceCache> placeList = new ArrayList<>(places.size());
            for (Place p : places) {
                PlacePeriod period = p.getPlanPlaces().stream()
                        .filter(pp -> pp != null && pp.getPlan() != null && planId.equals(pp.getPlan().getId()))
                        .map(PlanPlace::getPeriod)
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse(null);
                placeList.add(PlaceCache.fromEntity(p, period));
            }
            log.info("placeList : {}", placeList);

            // 3) 캐시 병합
            List<PlaceCache> cachePlaces = nlist(placeCacheService.getByPlanId(planId));
            log.info("cachePlaces : {}", cachePlaces);
            if (!cachePlaces.isEmpty()) {
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

            // 4) PlaceCache -> MapRouteReq.Location
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

            long count = nlist(route.locations()).stream()
                    .filter(l -> l != null && l.lat() != 0 && l.lng() != 0)
                    .count();

            // 5) GPT로 재정렬 (빈 입력도 허용)
            MapRouteByDayRes byDay = gptService.createRouteRequest(
                    route, transportation.toString(), count, categories, plan.getMoodSummary()
            );
            log.info("byDay (raw) : {}", byDay);

            if (byDay == null || byDay.days() == null) {
                // 완전 빈 결과 폴백
                return MapRouteByDayRes.builder()
                        .mode(MapRouteByDayRes.Mode.INDEXED)
                        .days(List.of(MapRouteByDayRes.Day.builder()
                                .index(1).date(null).locations(nlist(route.locations()))
                                .routes(Collections.emptyList()).build()))
                        .build();
            }

            // 6) DATED 정규화
            if (byDay.mode() != null && "DATED".equalsIgnoreCase(byDay.mode().name())) {
                for (var d : byDay.days()) {
                    if (d == null) continue;
                    var locs = d.locations();
                    if (locs == null) continue;
                    String dayDate = d.date();
                    for (int i = 0; i < locs.size(); i++) {
                        var loc = locs.get(i);
                        if (loc == null) continue;
                        var p = loc.period();
                        MapRouteReq.Period np = MapRouteReq.Period.of(
                                isBlank(dayDate) ? (p == null ? null : p.startedAt()) : dayDate,
                                isBlank(dayDate) ? (p == null ? null : p.finishedAt()) : dayDate,
                                (p != null) ? p.duration() : null,
                                (p != null && p.dayOrder() != null) ? p.dayOrder() : d.index()
                        );
                        locs.set(i, MapRouteReq.Location.of(
                                loc.lat(), loc.lng(), loc.name(), loc.placeType(), np,
                                loc.contentId(), loc.detailUrl(), loc.imageUrl(), loc.platform()
                        ));
                    }
                }
            }

            // 7) Kakao 보강 (예외/429 시 스킵)
            for (var d : byDay.days()) {
                if (d == null) continue;
                var dlocs = d.locations();
                if (dlocs == null || dlocs.isEmpty()) continue;

                double[] ctr = centroid(dlocs); // {0,0}이면 지오펜스 체크 스킵
                for (int i = 0; i < dlocs.size(); i++) {
                    var loc = dlocs.get(i);
                    if (loc == null || isBlank(loc.name())) continue;
                    boolean needSearch = isBlank(loc.contentId());
                    if (!needSearch) continue;

                    List<MapSearchRes> results;
                    try {
                        results = kakaoMapClient.searchByKeyword(loc.name());
                    } catch (Exception e) {
                        log.warn("Kakao search skipped ({}): {}", loc.name(), e.getMessage());
                        continue; // 검색 생략, 기존 값 유지
                    }
                    results = (results == null) ? List.of() : results;

                    List<MapSearchRes> candidates = results.stream()
                            .filter(r -> parseDoubleSafe(r.lat()) != 0 && parseDoubleSafe(r.lng()) != 0)
                            .filter(r -> {
                                if (ctr[0] == 0 && ctr[1] == 0) return true; // 센트로이드 불명 → 패스
                                return inGeofence(ctr[0], ctr[1], parseDoubleSafe(r.lat()), parseDoubleSafe(r.lng()));
                            })
                            .sorted(Comparator.comparingDouble(r ->
                                    haversineKm(loc.lat(), loc.lng(), parseDoubleSafe(r.lat()), parseDoubleSafe(r.lng()))
                            ))
                            .toList();

                    if (!candidates.isEmpty()) {
                        MapSearchRes best = candidates.get(0);
                        dlocs.set(i, MapRouteReq.Location.updateContent(
                                loc,
                                best.id(), best.detailUrl(), best.imageUrl(), "KAKAO",
                                parseDoubleSafe(best.lat()), parseDoubleSafe(best.lng()), best.name()
                        ));
                    } else {
                        String genId = genContentId();
                        dlocs.set(i, MapRouteReq.Location.updateContent(
                                loc, genId, null, null, "GENERATED",
                                loc.lat(), loc.lng(), loc.name()
                        ));
                        log.warn("contentId not found via Kakao. Generated contentId={} for name='{}'", genId, nz(loc.name()));
                    }
                }
            }

            // 8) Provider 선택
            Function<MapRouteReq, MapRouteRes> runner = switch (transportation) {
                case CAR -> carRouteProvider::route;
                case WALK -> walkRouteProvider::route;
                case BICYCLE -> bicycleRouteProvider::route;
                case TRANSPORT -> transitRouteProvider::route;
                default -> throw new GeneralException(ErrorStatus.TRANSPORT_TYPE_NOT_FOUND);
            };

            // 9) 날짜별 경로 계산 + PlaceInfo 보강 + DB 반영(널-세이프)
            List<MapRouteByDayRes.Day> enrichedDays = new ArrayList<>();
            Map<String, Place> placeByContentId = new HashMap<>();

            for (var day : byDay.days()) {
                if (day == null) continue;
                List<MapRouteReq.Location> dlocs = nlist(day.locations());
                List<MapRouteRes> routes = new ArrayList<>();

                if (dlocs.size() >= 2) {
                    MapRouteReq oneDayReq = MapRouteReq.of(plan.getRegion(), plan.getPeriod(), dlocs);
                    MapRouteRes dayRes = runner.apply(oneDayReq);

                    if (!RouteUtils.isEmpty(dayRes) && dayRes.legs() != null && !dayRes.legs().isEmpty()) {
                        double[] ctr = centroid(dlocs);
                        var filteredLegs = dayRes.legs().stream()
                                .filter(Objects::nonNull)
                                .filter(l -> {
                                    if (ctr[0] == 0 && ctr[1] == 0) return true; // 센트로이드 불명 → 필터 패스
                                    boolean okStart = inGeofence(ctr[0], ctr[1], l.startLat(), l.startLng());
                                    boolean okEnd = inGeofence(ctr[0], ctr[1], l.endLat(), l.endLng());
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

                            List<MapRouteRes> enrichedRoutes = enrichRoutesWithPlaceInfo(List.of(dayRes), dlocs);
                            routes.addAll(enrichedRoutes);
                        } else {
                            log.warn("day {}/{}: all legs removed by spike/geofence filter",
                                    nz(day.date()), String.valueOf(day.index()));
                        }
                    }
                }

                log.info("day {} routes: {}", nz(day.date()) + "/" + nz(String.valueOf(day.index())), routes);

                // DB 반영 (널-세이프)
                for (MapRouteRes r : routes) {
                    if (r == null || r.legs() == null) continue;
                    for (var leg : r.legs()) {
                        if (leg == null) continue;

                        var sp = leg.startPlace();
                        if (sp != null && !isBlank(sp.contentId())) {
                            Place startPlace = upsertPlaceEntity(
                                    placeByContentId, sp.contentId(), leg.startName(),
                                    leg.startLat(), leg.startLng(), safeType(sp.placeType()),
                                    sp.detailUrl(), sp.imageUrl()
                            );
                            upsertPlanPlacePeriod(plan, startPlace, sp.period());
                        }

                        var ep = leg.endPlace();
                        if (ep != null && !isBlank(ep.contentId())) {
                            Place endPlace = upsertPlaceEntity(
                                    placeByContentId, ep.contentId(), leg.endName(),
                                    leg.endLat(), leg.endLng(), safeType(ep.placeType()),
                                    ep.detailUrl(), ep.imageUrl()
                            );
                            upsertPlanPlacePeriod(plan, endPlace, ep.period());
                        }
                    }
                }

                enrichedDays.add(MapRouteByDayRes.Day.builder()
                        .date(day.date())
                        .index(day.index())
                        .locations(dlocs)
                        .routes(routes)
                        .build());
            }

            return MapRouteByDayRes.builder()
                    .mode(byDay.mode())
                    .days(enrichedDays)
                    .build();
        } catch (GeneralException e) {
            throw e;
        } catch (Exception e) {
            log.error("RouteService.getRoute failed: {}", e.getMessage(), e);
            throw e;
        }
    }

    private String genContentId() {
        return "GEN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    /** MapRouteRes의 각 Leg에 PlaceInfo가 없으면 locs로부터 역매핑하여 보강 */
    private List<MapRouteRes> enrichRoutesWithPlaceInfo(List<MapRouteRes> routes, List<MapRouteReq.Location> locs) {
        Map<String, MapRouteRes.Leg.PlaceInfo> idx = buildIndex(locs);
        List<MapRouteRes> out = new ArrayList<>(routes.size());
        for (var r : nlist(routes)) {
            if (r == null || r.legs() == null) continue;
            List<MapRouteRes.Leg> newLegs = new ArrayList<>(r.legs().size());
            for (var l : r.legs()) {
                if (l == null) continue;
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

    /** Location 목록으로부터 key(좌표, 이름) → PlaceInfo 인덱스 생성 */
    private Map<String, MapRouteRes.Leg.PlaceInfo> buildIndex(List<MapRouteReq.Location> locs) {
        Map<String, MapRouteRes.Leg.PlaceInfo> idx = new HashMap<>();
        for (var l : nlist(locs)) {
            if (l == null) continue;
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
            if (!isBlank(l.name())) {
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
        if (isBlank(contentId)) return null;

        // 1) 메모리 캐시
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
                .images(!isBlank(imageUrl) ? List.of(imageUrl) : Collections.emptyList())
                .build();
        placeRepository.save(newPlace);
        placeByContentId.put(contentId, newPlace);
        return newPlace;
    }

    private PlaceType parsePlaceType(String s) {
        if (isBlank(s) || "NULL".equalsIgnoreCase(s)) {
            return PlaceType.TOUR; // 기본값
        }
        try {
            return PlaceType.valueOf(s.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            log.warn("Unknown placeType: {}, defaulting to TOUR", s);
            return PlaceType.TOUR;
        }
    }

    /** (plan, place) PlanPlace upsert + period merge (널-세이프) */
    private void upsertPlanPlacePeriod(Plan plan, Place place, MapRouteRes.Leg.PlaceInfo.Period p) {
        if (plan == null || place == null) return;

        List<PlanPlace> planPlaces = plan.getPlanPlaces();
        planPlaces = (planPlaces == null) ? new ArrayList<>() : planPlaces;

        // 기존 PlanPlace 탐색
        PlanPlace existing = null;
        for (PlanPlace pp : planPlaces) {
            if (pp == null || pp.getPlace() == null) continue;
            if (pp.getPlace().getId() != null && place.getId() != null
                    && pp.getPlace().getId().equals(place.getId())) {
                existing = pp;
                break;
            }
        }

        // Period 변환(null-safe)
        LocalDate startedAt  = (p == null || isBlank(p.startedAt()))  ? null : LocalDate.parse(p.startedAt());
        LocalDate finishedAt = (p == null || isBlank(p.finishedAt())) ? null : LocalDate.parse(p.finishedAt());
        Integer duration     = (p == null) ? null : p.duration();
        Integer dayOrder     = (p == null) ? null : p.dayOrder();

        if (existing == null) {
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

            // plan.getPlanPlaces()가 null일 수 있어 add 전에 체크
            if (plan.getPlanPlaces() != null) {
                plan.getPlanPlaces().add(planPlace);
            }
            if (place.getPlanPlaces() != null) {
                place.getPlanPlaces().add(planPlace);
            }
        } else {
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

    /** 안전한 double 파싱 (null/예외 시 0.0) */
    private double parseDoubleSafe(String s) {
        try { return s == null ? 0.0 : Double.parseDouble(s); }
        catch (Exception e) { return 0.0; }
    }

    private String safeType(String s) {
        return (s == null) ? PlaceType.TOUR.name() : s.trim().toUpperCase(Locale.ROOT);
    }

    /** haversine 거리(km) */
    private double haversineKm(double lat1, double lng1, double lat2, double lng2) {
        final double R = 6371.0088;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat/2)*Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2))*
                        Math.sin(dLng/2)*Math.sin(dLng/2);
        return 2 * R * Math.asin(Math.sqrt(a));
    }

    /** locations의 중심좌표 */
    private double[] centroid(List<MapRouteReq.Location> locs) {
        double lat = 0, lng = 0; int n = 0;
        for (var l : nlist(locs)) {
            if (l == null) continue;
            if (l.lat() != 0 && l.lng() != 0) { lat += l.lat(); lng += l.lng(); n++; }
        }
        return (n == 0) ? new double[]{0, 0} : new double[]{lat / n, lng / n};
    }

    /** 지오펜스 내 여부 */
    private boolean inGeofence(double baseLat, double baseLng, double lat, double lng) {
        return haversineKm(baseLat, baseLng, lat, lng) <= KM_GEOFENCE;
    }
}