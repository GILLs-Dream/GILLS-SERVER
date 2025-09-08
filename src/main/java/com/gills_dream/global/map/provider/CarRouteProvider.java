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

/**
 * 자동차 길찾기(단건) 기반 Provider
 * - Kakao 자동차 길찾기 경유지 제한: 최대 5개 (즉, 한 요청에 포인트 최대 7개: origin + ≤5 waypoints + destination)
 * - 이를 초과하면 조각내어 여러 번 호출 후 legs를 이어붙인다.
 */
@Component
@RequiredArgsConstructor
public class CarRouteProvider implements RouteProvider {

    private final KakaoMobilityClient kakao;

    @Override
    public TransportType supports() { return TransportType.CAR; }

    private static final int MAX_WAYPOINTS_PER_REQ = 5; // Kakao 자동차 길찾기 경유지 제한
    private static final double DUP_EPSILON = 1e-6;     // 연속 중복 좌표 판단 임계값

    @Override
    public MapRouteRes route(MapRouteReq req) {
        if (RouteUtils.invalid(req)) return RouteUtils.empty();

        // 1) 입력 정제 (연속 중복 좌표 제거)
        List<MapRouteReq.Location> locs = sanitize(req.locations());
        if (locs.size() < 2) return RouteUtils.empty();

        // 2) 경유지 제한을 고려하여 조각내어 순차 호출
        List<MapRouteRes.Leg> allLegs = new ArrayList<>();
        double totalDist = 0;
        double totalTime = 0;

        int n = locs.size();
        int i = 0;
        while (i < n - 1) {
            // [i .. j] 조각 하나를 구성: waypoints 개수 (i+1 .. j-1) ≤ 5
            int maxJ = Math.min(i + 1 + MAX_WAYPOINTS_PER_REQ, n - 1);
            int j = maxJ;

            MapRouteReq.Location origin = locs.get(i);
            MapRouteReq.Location dest   = locs.get(j);

            MobilityRouteReq singleReq = toSingleRequest(origin, dest, locs, i + 1, j - 1);
            MobilityRouteRes res = kakao.findMultiRoute(singleReq);

            if (res == null || res.routes() == null || res.routes().isEmpty()) {
                // 실패 시라도 day가 비지 않게 0거리 leg 추가 (원하면 스킵 처리 가능)
                allLegs.add(RouteUtils.legSingleMode(
                        origin.name(), origin.lat(), origin.lng(),
                        dest.name(),   dest.lat(),   dest.lng(),
                        "CAR", 0, 0
                ));
                i = j;
                continue;
            }

            var best = res.routes().stream()
                    .min(Comparator.comparingInt(r -> r.summary() != null ? r.summary().duration() : Integer.MAX_VALUE))
                    .orElse(res.routes().get(0));

            double dist = best.summary() != null ? best.summary().distance() : 0;
            double time = best.summary() != null ? best.summary().duration() : 0;
            totalDist += dist;
            totalTime += time;

            // 섹션/포인트로 세부 구간 legs 복원
            allLegs.addAll(buildLegsFromBest(best));

            i = j; // 다음 조각으로 이동
        }

        return RouteUtils.build(totalDist, totalTime, allLegs);
    }

    /** origin, destination, (inclusive) waypoint index 범위를 받아 단건 요청을 만든다. */
    private MobilityRouteReq toSingleRequest(
            MapRouteReq.Location origin,
            MapRouteReq.Location destination,
            List<MapRouteReq.Location> all,
            int wpStartInclusive,
            int wpEndInclusive
    ) {
        List<MobilityRouteReq.Waypoint> wps = new ArrayList<>();
        for (int k = wpStartInclusive; k <= wpEndInclusive; k++) {
            if (k < 0 || k >= all.size()) continue;
            var wp = all.get(k);
            if (wp == null) continue;
            wps.add(new MobilityRouteReq.Waypoint(wp.name(), wp.lng(), wp.lat())); // Kakao: x=lng, y=lat
        }

        var o = new MobilityRouteReq.Place(origin.name(), origin.lng(), origin.lat(), null);
        var d = new MobilityRouteReq.Place(destination.name(), destination.lng(), destination.lat(), null);

        return new MobilityRouteReq(
                o, d, wps,
                "RECOMMEND", null, 0, false, false,
                1, "GASOLINE", false, false
        );
    }

    /** Kakao 응답의 best route에서 sections/points를 읽어 MapRouteRes.Leg 리스트로 변환 */
    private List<MapRouteRes.Leg> buildLegsFromBest(MobilityRouteRes.Route best) {
        List<MapRouteRes.Leg> legs = new ArrayList<>();
        List<MobilityRouteRes.Place> points = new ArrayList<>();

        if (best.summary() != null) {
            if (best.summary().origin() != null)       points.add(best.summary().origin());
            if (best.summary().waypoints() != null)    points.addAll(best.summary().waypoints());
            if (best.summary().destination() != null)  points.add(best.summary().destination());
        }

        var sections = best.sections() != null ? best.sections() : List.<MobilityRouteRes.Section>of();
        int pairCnt = Math.min(sections.size(), Math.max(points.size() - 1, 0));

        for (int s = 0; s < pairCnt; s++) {
            var sec = sections.get(s);
            var fr  = points.get(s);
            var to  = points.get(s + 1);
            legs.add(RouteUtils.legSingleMode(
                    fr != null ? fr.name() : null, fr != null ? fr.y() : null, fr != null ? fr.x() : null,
                    to != null ? to.name() : null, to != null ? to.y() : null, to != null ? to.x() : null,
                    "CAR", sec.distance(), sec.duration()
            ));
        }
        return legs;
    }

    /** 연속 중복 좌표 제거 (lat/lng가 거의 동일한 포인트가 붙어 있을 때 제거) */
    private List<MapRouteReq.Location> sanitize(List<MapRouteReq.Location> src) {
        List<MapRouteReq.Location> out = new ArrayList<>();
        MapRouteReq.Location prev = null;
        for (MapRouteReq.Location cur : src) {
            if (cur == null) continue;
            if (cur.lat() == 0.00 || cur.lng() == 0.00) continue;
            if (prev != null &&
                    Math.abs(prev.lat() - cur.lat()) < DUP_EPSILON &&
                    Math.abs(prev.lng() - cur.lng()) < DUP_EPSILON) {
                // 연속 중복 제거
                continue;
            }
            out.add(cur);
            prev = cur;
        }
        return out;
    }
}