package com.gills_dream.global.map.provider;

import com.gills_dream.domain.map.dto.MapRouteReq;
import com.gills_dream.domain.map.dto.MapRouteRes;
import com.gills_dream.domain.plan.enumerate.TransportType;
import com.gills_dream.global.map.openRouteService.ORSRouteClient;
import com.gills_dream.global.map.openRouteService.dto.ORSRouteReq;
import com.gills_dream.global.map.openRouteService.dto.ORSRouteRes;
import com.gills_dream.global.map.util.RouteUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BicycleRouteProvider implements RouteProvider {

    private final ORSRouteClient ors;

    @Override public TransportType supports() { return TransportType.BICYCLE; }

    /**
     * 자전거 경로 조회
     * @param req
     * @return MapRouteRes
     */
    @Override
    public MapRouteRes route(MapRouteReq req) {
        if (RouteUtils.invalid(req)) return RouteUtils.empty();

        double totalDist = 0d, totalTime = 0d;
        List<MapRouteRes.Leg> legs = new ArrayList<>();

        // 위치 쌍마다 경로 조회
        for (int[] p : RouteUtils.segmentPairs(req)) {
            var from = req.locations().get(p[0]);
            var to   = req.locations().get(p[1]);

            ORSRouteReq segReq = new ORSRouteReq(from.lng(), from.lat(), to.lng(), to.lat());
            ORSRouteRes segRes = ors.findBicycleRoute(segReq);
            if (segRes == null || segRes.features() == null || segRes.features().isEmpty()) continue;

            double segDist = 0d, segTime = 0d;
            for (var f : segRes.features()) {
                if (f.properties() != null && f.properties().segments() != null) {
                    for (var s : f.properties().segments()) {
                        segDist += s.distance();
                        segTime += s.duration();
                    }
                }
            }

            totalDist += segDist; totalTime += segTime;
            legs.add(RouteUtils.leg(
                    from.name(), from.lat(), from.lng(),
                    to.name(),   to.lat(),   to.lng(),
                    "BICYCLE", segDist, segTime
            ));
        }

        return RouteUtils.build(totalDist, totalTime, legs);
    }
}