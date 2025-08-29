package com.gills_dream.domain.map.service;

import com.gills_dream.domain.map.dto.MapRouteReq;
import com.gills_dream.domain.map.dto.MapRouteRes;
import com.gills_dream.global.map.provider.BicycleRouteProvider;
import com.gills_dream.global.map.provider.CarRouteProvider;
import com.gills_dream.global.map.provider.TransitRouteProvider;
import com.gills_dream.global.map.provider.WalkRouteProvider;
import com.gills_dream.domain.plan.enumerate.TransportType;
import com.gills_dream.global.apiPayload.code.status.ErrorStatus;
import com.gills_dream.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RouteService {

    private final CarRouteProvider carRouteProvider;
    private final WalkRouteProvider walkRouteProvider;
    private final BicycleRouteProvider bicycleRouteProvider;
    private final TransitRouteProvider transitRouteProvider;

    /**
     * 경로 조회
     * @param transportation
     * @param routeReq
     * @return MapRouteRes
     * @throws GeneralException
     */
    public MapRouteRes getRoute(TransportType transportation, MapRouteReq routeReq) throws GeneralException {
        if (routeReq == null || routeReq.locations() == null || routeReq.locations().size() < 2) {
            throw new GeneralException(ErrorStatus.INVALID_ROUTE_REQUEST);
        }

        return switch (transportation) {
            case CAR -> carRouteProvider.route(routeReq);
            case WALK -> walkRouteProvider.route(routeReq);
            case BICYCLE -> bicycleRouteProvider.route(routeReq);
            case BUS, SUBWAY -> transitRouteProvider.route(routeReq);
            default -> throw new GeneralException(ErrorStatus.TRANSPORT_TYPE_NOT_FOUND);
        };
    }
}