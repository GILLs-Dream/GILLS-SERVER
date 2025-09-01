package com.gills_dream.global.map.provider;

import com.gills_dream.domain.map.dto.MapRouteReq;
import com.gills_dream.domain.map.dto.MapRouteRes;
import com.gills_dream.domain.plan.enumerate.TransportType;
import com.gills_dream.global.map.kakao.KakaoMobilityClient;
import com.gills_dream.global.map.kakao.dto.MobilityRouteReq;
import com.gills_dream.global.map.kakao.dto.MobilityRouteRes;
import com.gills_dream.global.map.util.RouteUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CarRouteProvider implements RouteProvider {

    private final KakaoMobilityClient kakao;

    @Override public TransportType supports() { return TransportType.CAR; }

    /**
     * 자동차 경로 조회
     * @param req
     * @return MapRouteRes
     */
    @Override
    public MapRouteRes route(MapRouteReq req) {
        if (RouteUtils.invalid(req)) return RouteUtils.empty();

        MobilityRouteReq mReq = toMobilityReq(req);
        MobilityRouteRes res = kakao.findMultiRoute(mReq);
        if (res == null || res.routes() == null || res.routes().isEmpty()) return RouteUtils.empty();

        // 가장 빠른 경로 선택
        var route = res.routes().stream()
                .min(Comparator.comparingInt(r -> r.summary() != null ? r.summary().duration() : Integer.MAX_VALUE))
                .orElse(res.routes().get(0));

        double totalDist = route.summary() != null ? route.summary().distance() : 0;
        double totalTime = route.summary() != null ? route.summary().duration() : 0;

        // 경로 지점들 (출발지, 경유지들, 도착지)
        List<MobilityRouteRes.Place> points = new ArrayList<>();
        if (route.summary() != null) {
            if (route.summary().origin() != null) points.add(route.summary().origin());
            if (route.summary().waypoints() != null) points.addAll(route.summary().waypoints());
            if (route.summary().destination() != null) points.add(route.summary().destination());
        }

        var sections  = route.sections() != null ? route.sections() : List.<MobilityRouteRes.Section>of();
        int pairCount = Math.min(sections.size(), Math.max(points.size() - 1, 0));

        // 구간별 경로
        List<MapRouteRes.Leg> legs = new ArrayList<>(pairCount);
        for (int i = 0; i < pairCount; i++) {
            var s = sections.get(i);
            var from = points.get(i);
            var to   = points.get(i + 1);

            legs.add(RouteUtils.leg(
                    from != null ? from.name() : null, from != null ? from.y() : null, from != null ? from.x() : null,
                    to   != null ? to.name()   : null, to   != null ? to.y()   : null, to   != null ? to.x()   : null,
                    "CAR", s.distance(), s.duration()
            ));
        }

        return RouteUtils.build(totalDist, totalTime, legs);
    }

    /**
     * MapRouteReq -> MobilityRouteReq 변환
     * @param req
     * @return MobilityRouteReq
     */
    private MobilityRouteReq toMobilityReq(MapRouteReq req) {
        var locs = req.locations();
        var start = locs.get(0);
        var end   = locs.get(locs.size() - 1);

        List<MobilityRouteReq.Waypoint> wps = new ArrayList<>();
        for (int i = 1; i < locs.size() - 1; i++) {
            var wp = locs.get(i);
            wps.add(new MobilityRouteReq.Waypoint(wp.name(), wp.lng(), wp.lat())); // x=lng, y=lat
        }

        var origin      = new MobilityRouteReq.Place(start.name(), start.lng(), start.lat(), null);
        var destination = new MobilityRouteReq.Place(end.name(),   end.lng(),   end.lat(),   null);

        return new MobilityRouteReq(
                origin, destination, wps,
                "RECOMMEND", null, 0, false, false,
                1, "GASOLINE", false, false
        );
    }
}