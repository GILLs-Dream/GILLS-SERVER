package com.gills_dream.global.map.util;

import com.gills_dream.domain.map.dto.MapRouteReq;
import com.gills_dream.domain.map.dto.MapRouteRes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class RouteUtils {

    private RouteUtils() {}

    public static MapRouteRes empty() {
        return MapRouteRes.builder().distance(0).duration(0).polyline(null).legs(List.of()).build();
    }

    public static boolean isEmpty(MapRouteRes res) {
        return res == null || res.legs() == null || res.legs().isEmpty()
                || (res.distance() == 0 && res.duration() == 0);
    }

    public static MapRouteRes build(double totalDist, double totalTime, List<MapRouteRes.Leg> legs) {
        return MapRouteRes.builder()
                .distance(totalDist)
                .duration(totalTime)
                .polyline(null)
                .legs(legs == null ? List.of() : legs)
                .build();
    }

    /** ★ 단일 모드 → 리스트로 감싸는 헬퍼 (CAR/WALK 등에서 편하게 사용) */
    public static MapRouteRes.Leg legSingleMode(
            String startName, Double startLat, Double startLng,
            String endName,   Double endLat,   Double endLng,
            String mode, double distance, double duration
    ) {
        return leg(startName, startLat, startLng, endName, endLat, endLng,
                List.of(normalizeMode(mode)), distance, duration);
    }

    /** ★ 다중 모드 레그 생성 */
    public static MapRouteRes.Leg leg(
            String startName, Double startLat, Double startLng,
            String endName,   Double endLat,   Double endLng,
            List<String> modes, double distance, double duration
    ) {
        return MapRouteRes.Leg.builder()
                .startName(startName)
                .startLat(startLat == null ? 0.0 : startLat)
                .startLng(startLng == null ? 0.0 : startLng)
                .endName(endName)
                .endLat(endLat == null ? 0.0 : endLat)
                .endLng(endLng == null ? 0.0 : endLng)
                .modes(modes == null ? List.of() : modes.stream()
                        .filter(Objects::nonNull)
                        .map(RouteUtils::normalizeMode)
                        .distinct()
                        .toList())
                .distance(distance)
                .duration(duration)
                .build();
    }

    /** ★ 모드 표준화 (세분화 유지) */
    public static String normalizeMode(String raw) {
        if (raw == null) return "WALK";
        String m = raw.trim().toUpperCase();
        if (m.contains("WALK"))    return "WALK";
        if (m.contains("BUS"))     return "BUS";
        if (m.contains("SUBWAY") || m.contains("METRO")) return "SUBWAY";
        if (m.contains("BICYCLE") || m.contains("BIKE")) return "BICYCLE";
        if (m.contains("CAR") || m.contains("DRIVE"))    return "CAR";
        return m;
    }

    /** ★ 레그들에서 모드 수집 (순서 보존) */
    public static List<String> collectModes(MapRouteRes res) {
        var ordered = new java.util.LinkedHashSet<String>();
        if (res != null && res.legs() != null) {
            for (var l : res.legs()) {
                if (l.modes() != null) {
                    for (var m : l.modes()) ordered.add(normalizeMode(m));
                }
            }
        }
        return new java.util.ArrayList<>(ordered);
    }

    public static List<int[]> segmentPairs(MapRouteReq req) {
        var out = new ArrayList<int[]>();
        for (int i = 0; i < req.locations().size() - 1; i++) out.add(new int[]{i, i + 1});
        return out;
    }

    public static boolean invalid(MapRouteReq req) {
        return req == null || req.locations() == null || req.locations().size() < 2;
    }
}