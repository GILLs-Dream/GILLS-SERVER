package com.gills_dream.domain.map.service;

import com.gills_dream.domain.gpt.service.GptService;
import com.gills_dream.domain.map.dto.MapRouteReq;
import com.gills_dream.domain.map.dto.MapRouteRes;
import com.gills_dream.domain.place.service.PlaceCacheService;
import com.gills_dream.domain.place.vo.PlaceCache;
import com.gills_dream.global.map.provider.BicycleRouteProvider;
import com.gills_dream.global.map.provider.CarRouteProvider;
import com.gills_dream.global.map.provider.TransitRouteProvider;
import com.gills_dream.global.map.provider.WalkRouteProvider;
import com.gills_dream.domain.plan.enumerate.TransportType;
import com.gills_dream.global.apiPayload.code.status.ErrorStatus;
import com.gills_dream.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

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

    /**
     * 경로 조회
     * @param transportation
     * @param planId
     * @return MapRouteRes
     * @throws GeneralException
     */
    public MapRouteRes getRoute(Long planId, TransportType transportation) throws GeneralException {
        List<PlaceCache> placeList = placeCacheService.getByPlanId(planId);

        if (placeList == null || placeList.size() < 2) {
            throw new GeneralException(ErrorStatus.INVALID_ROUTE_REQUEST);
        }

        // PlaceCache → MapRouteReq.Location 변환
        List<MapRouteReq.Location> locations = placeList.stream()
                .map(p -> MapRouteReq.Location.of(
                        Double.parseDouble(p.lat()),
                        Double.parseDouble(p.lng()),
                        p.name()
                ))
                .toList();


        MapRouteReq route = MapRouteReq.of(locations);
        Long count = route.locations().stream()
                .filter(l -> l.lat() != 0 && l.lng() != 0)
                .count();
        MapRouteReq routeReq = gptService.createRouteRequest(route, TransportType.toCategory(transportation), count);

        return switch (transportation) {
            case CAR -> carRouteProvider.route(routeReq);
            case WALK -> walkRouteProvider.route(routeReq);
            case BICYCLE -> bicycleRouteProvider.route(routeReq);
            case BUS, SUBWAY -> transitRouteProvider.route(routeReq);
            default -> throw new GeneralException(ErrorStatus.TRANSPORT_TYPE_NOT_FOUND);
        };
    }
}