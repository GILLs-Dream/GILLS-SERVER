package com.gills_dream.global.map.provider;

import com.gills_dream.domain.map.dto.MapRouteReq;
import com.gills_dream.domain.map.dto.MapRouteRes;
import com.gills_dream.domain.plan.enumerate.TransportType;
import com.gills_dream.global.map.tmap.TmapRouteClient;
import com.gills_dream.global.map.tmap.dto.PedestrianRouteReq;
import com.gills_dream.global.map.tmap.dto.PedestrianRouteRes;
import com.gills_dream.global.map.util.RouteUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WalkRouteProvider implements RouteProvider {

    private static final double NEAR_THRESHOLD_M = 100.0;   // 100m 미만은 TMAP 호출 스킵
    private static final double NEAR_DURATION_SEC = 60.0;   // “1분 미만으로 간주”
    private final TmapRouteClient tmap;

    @Override public TransportType supports() { return TransportType.WALK; }

    @Override
    public MapRouteRes route(MapRouteReq req) {
        if (RouteUtils.invalid(req)) return RouteUtils.empty();

        double totalDist = 0, totalTime = 0;
        List<MapRouteRes.Leg> legs = new ArrayList<>();

        for (int[] p : RouteUtils.segmentPairs(req)) {
            var from = req.locations().get(p[0]);
            var to   = req.locations().get(p[1]);

            double d = distanceMeters(from.lat(), from.lng(), to.lat(), to.lng());

            // 1) 가까운 구간(<=100m): TMAP 호출 없이 1분 미만으로 간주
            if (d <= NEAR_THRESHOLD_M) {
                log.debug("[WALK][NEAR] {} -> {} : {} m (<=100m) → {} sec shortcut",
                        from.name(), to.name(), Math.round(d), (int) NEAR_DURATION_SEC);
                totalDist += d;
                totalTime += NEAR_DURATION_SEC;

                legs.add(RouteUtils.legSingleMode(
                        from.name(), from.lat(), from.lng(),
                        to.name(),   to.lat(),   to.lng(),
                        "WALK", d, NEAR_DURATION_SEC
                ));
                continue;
            }

            // 2) 정상 구간: TMAP 호출
            try {
                PedestrianRouteReq segReq = PedestrianRouteReq.of(
                        from.lng(), from.lat(), from.name(),
                        to.lng(),   to.lat(),   to.name()
                );
                PedestrianRouteRes segRes = tmap.findPedestrianRoute(segReq);

                if (segRes == null || segRes.features() == null || segRes.features().isEmpty()) {
                    log.warn("[WALK][EMPTY] {} -> {} : TMAP 응답 없음. 스킵.", from.name(), to.name());
                    continue;
                }

                double segDist = 0, segTime = 0;
                for (var f : segRes.features()) {
                    var prop = f.properties();
                    if (prop != null) {
                        if (prop.distance() != null) segDist += prop.distance();
                        if (prop.time() != null)     segTime += prop.time();
                    }
                }

                totalDist += segDist;
                totalTime += segTime;

                legs.add(RouteUtils.legSingleMode(
                        from.name(), from.lat(), from.lng(),
                        to.name(),   to.lat(),   to.lng(),
                        "WALK", segDist, segTime
                ));
            } catch (RuntimeException e) {
                // 3) 에러 우회: “waypoints too near”, “unsupported section” 등 400류 발생 시 근거리 취급 또는 스킵
                String msg = e.getMessage() == null ? "" : e.getMessage();
                if (msg.contains("too near") || msg.contains("1007") || msg.contains("NoServiceArea") || msg.contains("3102")) {
                    // 근거리로 취급 (보수적으로 1분) — 혹시 실제 d가 꽤 멀면 로그로만 남김
                    log.warn("[WALK][FALLBACK-NEAR] {} -> {} : TMAP 오류 우회. msg={}, d≈{} m → {} sec",
                            from.name(), to.name(), msg, Math.round(d), (int) NEAR_DURATION_SEC);
                    totalDist += d;
                    totalTime += NEAR_DURATION_SEC;

                    legs.add(RouteUtils.legSingleMode(
                            from.name(), from.lat(), from.lng(),
                            to.name(),   to.lat(),   to.lng(),
                            "WALK", d, NEAR_DURATION_SEC
                    ));
                } else {
                    log.error("[WALK][SKIP] {} -> {} : TMAP 호출 실패. msg={}", from.name(), to.name(), msg);
                    // 실패 구간은 일정에서 생략 (다음 세그먼트 진행)
                }
            }
        }

        return RouteUtils.build(totalDist, totalTime, legs);
    }

    /** 하버사인 거리 (meter) */
    private static double distanceMeters(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371000.0; // meters
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));
        return R * c;
    }
}