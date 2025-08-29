package com.gills_dream.domain.map.provider;

import com.gills_dream.domain.map.dto.MapRouteReq;
import com.gills_dream.domain.map.dto.MapRouteRes;
import com.gills_dream.domain.plan.enumerate.TransportType;
import com.gills_dream.global.map.tmap.TmapRouteClient;
import com.gills_dream.global.map.tmap.dto.PedestrianRouteReq;
import com.gills_dream.global.map.tmap.dto.PedestrianRouteRes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class WalkRouteProvider implements RouteProvider {

    private final TmapRouteClient tmap;

    @Override public TransportType supports() { return TransportType.WALK; }

    /**
     * 도보 경로 조회
     * @param req
     * @return MapRouteRes
     */
    @Override
    public MapRouteRes route(MapRouteReq req) {
        if (RouteUtils.invalid(req)) return RouteUtils.empty();

        double totalDist = 0, totalTime = 0;
        List<MapRouteRes.Leg> legs = new ArrayList<>();

        for (int[] p : RouteUtils.segmentPairs(req)) {
            var from = req.locations().get(p[0]);
            var to   = req.locations().get(p[1]);

            PedestrianRouteReq segReq = PedestrianRouteReq.of(
                    from.lng(), from.lat(), from.name(),
                    to.lng(),   to.lat(),   to.name()
            );
            PedestrianRouteRes segRes = tmap.findPedestrianRoute(segReq);
            if (segRes == null || segRes.features() == null || segRes.features().isEmpty()) continue;

            double segDist = 0, segTime = 0;
            for (var f : segRes.features()) {
                var prop = f.properties();
                if (prop != null) {
                    if (prop.distance() != null) segDist += prop.distance();
                    if (prop.time() != null)     segTime += prop.time();
                }
            }

            totalDist += segDist; totalTime += segTime;
            legs.add(RouteUtils.leg(
                    from.name(), from.lat(), from.lng(),
                    to.name(),   to.lat(),   to.lng(),
                    "WALK", segDist, segTime
            ));
        }

        return RouteUtils.build(totalDist, totalTime, legs);
    }
}