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

    @Override public TransportType supports() { return TransportType.BUS; } // SUBWAY도 동일 Provider 사용 시 그대로

    @Override
    public MapRouteRes route(MapRouteReq req) {
        if (RouteUtils.invalid(req)) return RouteUtils.empty();

        double totalDist = 0, totalTime = 0;
        List<MapRouteRes.Leg> legs = new ArrayList<>();

        for (int[] p : RouteUtils.segmentPairs(req)) {
            var from = req.locations().get(p[0]);
            var to   = req.locations().get(p[1]);

            var segReq = TransitRouteReq.builder()
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
            // ★ 모드 모으기: 순서 보존 + 중복 제거
            java.util.LinkedHashSet<String> modes = new java.util.LinkedHashSet<>();

            if (route.legs() != null) {
                for (var l : route.legs()) {
                    segDist += l.distance();
                    segTime += l.sectionTime();
                    modes.add(RouteUtils.normalizeMode(l.mode()));
                }
            }

            // ★ 같은 출발-도착(한 구간) → 한 개의 Leg, 여러 모드
            legs.add(RouteUtils.leg(
                    from.name(), from.lat(), from.lng(),
                    to.name(),   to.lat(),   to.lng(),
                    new java.util.ArrayList<>(modes),  // e.g. ["WALK","BUS","SUBWAY"]
                    segDist, segTime
            ));

            totalDist += segDist;
            totalTime += segTime;
        }

        return RouteUtils.build(totalDist, totalTime, legs);
    }
}