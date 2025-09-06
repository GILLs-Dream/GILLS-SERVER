package com.gills_dream.domain.map.service;

import com.gills_dream.domain.gpt.service.GptService;
import com.gills_dream.domain.map.dto.MapRouteByDayRes;
import com.gills_dream.domain.map.dto.MapRouteReq;
import com.gills_dream.domain.map.dto.MapRouteRes;
import com.gills_dream.domain.place.service.PlaceCacheService;
import com.gills_dream.domain.place.vo.PlaceCache;
import com.gills_dream.domain.plan.entity.Plan;
import com.gills_dream.domain.plan.enumerate.TransportType;
import com.gills_dream.domain.plan.repository.PlanRepository;
import com.gills_dream.global.apiPayload.code.status.ErrorStatus;
import com.gills_dream.global.apiPayload.exception.GeneralException;
import com.gills_dream.global.map.provider.BicycleRouteProvider;
import com.gills_dream.global.map.provider.CarRouteProvider;
import com.gills_dream.global.map.provider.TransitRouteProvider;
import com.gills_dream.global.map.provider.WalkRouteProvider;
import com.gills_dream.global.map.util.RouteUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

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
    private final PlanRepository planRepository;

    /**
     * 경로 조회 (GPT가 만든 날짜/인덱스별 locations로 provider 호출 → routes만 채워서 반환)
     */
    public MapRouteByDayRes getRoute(Long planId, TransportType transportation) throws GeneralException {
        // 1) planId로 장소 리스트 조회
        Plan plan = planRepository.findById(planId);
        List<PlaceCache> placeList = placeCacheService.getByPlanId(planId);
        if (placeList == null || placeList.size() < 2) {
            throw new GeneralException(ErrorStatus.INVALID_ROUTE_REQUEST);
        }

        // 2) PlaceCache → MapRouteReq.Location 변환
        List<MapRouteReq.Location> locations = placeList.stream()
                .map(p -> MapRouteReq.Location.of(
                        parseDoubleSafe(p.lat()),
                        parseDoubleSafe(p.lng()),
                        p.name(),
                        p.placeType() != null ? p.placeType() : null,  // enum이면 .name()으로 변환
                        p.period() == null ? null :
                                MapRouteReq.Period.of(
                                        p.period().startedAt(),
                                        p.period().finishedAt(),
                                        p.period().duration(),
                                        p.period().dayOrder()
                                )
                ))
                .toList();

        // 3) 유효 좌표 개수
        MapRouteReq route = MapRouteReq.of(locations);
        long count = route.locations().stream()
                .filter(l -> l.lat() != 0 && l.lng() != 0)
                .count();

        // 4) GPT로 날짜/인덱스별 재정렬 결과 받기
        MapRouteByDayRes byDay = gptService.createRouteRequest(
                route, transportation.getDescription(), count
        );

        // 5) Provider 실행기 선택
        Function<MapRouteReq, MapRouteRes> runner = switch (transportation) {
            case CAR     -> carRouteProvider::route;
            case WALK    -> walkRouteProvider::route;
            case BICYCLE -> bicycleRouteProvider::route;
            case TRANSPORT -> transitRouteProvider::route; // 대중교통
            default -> throw new GeneralException(ErrorStatus.TRANSPORT_TYPE_NOT_FOUND);
        };

        // 6) 날짜별로 Provider 호출해서 routes 채우기
        List<MapRouteByDayRes.Day> enrichedDays = new ArrayList<>();
        for (var day : byDay.days()) {
            List<MapRouteRes> routes = new ArrayList<>();

            if (day.locations() != null && day.locations().size() >= 2) {
                MapRouteReq oneDayReq = MapRouteReq.of(day.locations());
                MapRouteRes dayRes = runner.apply(oneDayReq);

                if (!RouteUtils.isEmpty(dayRes)) {
                    routes.add(dayRes);
                }
            }

            enrichedDays.add(MapRouteByDayRes.Day.builder()
                    .date(day.date())
                    .index(day.index())
                    .locations(day.locations())
                    .routes(routes)
                    .build());
        }

        MapRouteByDayRes mapRouteByDayRes = MapRouteByDayRes.builder()
                .mode(byDay.mode())
                .days(enrichedDays)
                .build();

        plan.updateGeneratedRoute(mapRouteByDayRes);

        return mapRouteByDayRes;
    }

    private double parseDoubleSafe(String s) {
        try { return s == null ? 0.0 : Double.parseDouble(s); }
        catch (Exception e) { return 0.0; }
    }
}