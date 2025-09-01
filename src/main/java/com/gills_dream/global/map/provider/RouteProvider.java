package com.gills_dream.global.map.provider;

import com.gills_dream.domain.map.dto.MapRouteReq;
import com.gills_dream.domain.map.dto.MapRouteRes;
import com.gills_dream.domain.plan.enumerate.TransportType;

public interface RouteProvider {
    TransportType supports();
    MapRouteRes route(MapRouteReq request);
}