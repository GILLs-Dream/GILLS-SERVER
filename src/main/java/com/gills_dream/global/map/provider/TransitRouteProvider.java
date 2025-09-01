package com.gills_dream.global.map.provider;

import com.gills_dream.domain.map.dto.MapRouteReq;
import com.gills_dream.domain.map.dto.MapRouteRes;
import com.gills_dream.domain.plan.enumerate.TransportType;
import com.gills_dream.global.map.tmap.TmapRouteClient;
import com.gills_dream.global.map.tmap.dto.TransitRouteReq;
import com.gills_dream.global.map.tmap.dto.TransitRawRes;
import com.gills_dream.global.map.util.RouteUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TransitRouteProvider implements RouteProvider {

    private final TmapRouteClient tmap;

    @Override public TransportType supports() { return TransportType.BUS; } // SUBWAY도 이 Provider 사용

    /**
     * 대중교통 경로 조회
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

            TransitRouteReq segReq = TransitRouteReq.builder()
                    .startX(String.valueOf(from.lng()))
                    .startY(String.valueOf(from.lat()))
                    .endX(String.valueOf(to.lng()))
                    .endY(String.valueOf(to.lat()))
                    .build();

            TransitRawRes raw = tmap.findTransitRoute(segReq);
            if (raw == null || raw.metaData() == null || raw.metaData().plan() == null
                    || raw.metaData().plan().itineraries() == null || raw.metaData().plan().itineraries().isEmpty()) {
                continue;
            }

            var route = raw.metaData().plan().itineraries().stream()
                    .min(Comparator.comparingInt(TransitRawRes.Plan.TransitRouteRes::totalTime))
                    .orElse(raw.metaData().plan().itineraries().get(0));

            double segDist = 0, segTime = 0;
            if (route.legs() != null) {
                for (var l : route.legs()) {
                    segDist += l.distance();
                    segTime += l.sectionTime();

                    legs.add(RouteUtils.leg(
                            l.startName() != null ? l.startName() : from.name(),
                            from.lat(), from.lng(),
                            l.endName()   != null ? l.endName()   : to.name(),
                            to.lat(), to.lng(),
                            RouteUtils.normalizeMode(l.mode()),  // BUS/SUBWAY → TRANSIT 매핑도 같이
                            l.distance(), l.sectionTime()
                    ));
                }
            }

            totalDist += segDist; totalTime += segTime;
        }

        return RouteUtils.build(totalDist, totalTime, legs);
    }
}