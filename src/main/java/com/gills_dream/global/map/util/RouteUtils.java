package com.gills_dream.global.map.util;

import com.gills_dream.domain.map.dto.MapRouteReq;
import com.gills_dream.domain.map.dto.MapRouteRes;

import java.util.ArrayList;
import java.util.List;

public final class RouteUtils {

    private RouteUtils() {}

    /**
     * 요청 유효성 검사
     * @param req
     * @return boolean
     */
    public static boolean invalid(MapRouteReq req) {
        return req == null || req.locations() == null || req.locations().size() < 2;
    }

    /**
     * 빈 경로
     * @return MapRouteRes
     */
    public static MapRouteRes empty() {
        return MapRouteRes.builder()
                .distance(0).duration(0).polyline(null).legs(List.of())
                .build();
    }

    public static boolean isEmpty(MapRouteRes res) {
        return res == null
                || res.legs() == null
                || res.legs().isEmpty()
                || (res.distance() == 0 && res.duration() == 0);
    }

    /**
     * 경로 응답 생성
     * @param totalDist
     * @param totalTime
     * @param legs
     * @return MapRouteRes
     */
    public static MapRouteRes build(double totalDist, double totalTime, List<MapRouteRes.Leg> legs) {
        return MapRouteRes.builder()
                .distance(totalDist)
                .duration(totalTime)
                .polyline(null)
                .legs(legs == null ? List.of() : legs)
                .build();
    }

    /**
     * 구간 응답 생성
     * @param startName
     * @param startLat
     * @param startLng
     * @param endName
     * @param endLat
     * @param endLng
     * @param mode
     * @param distance
     * @param duration
     * @return MapRouteRes.Leg
     */
    public static MapRouteRes.Leg leg(String startName, Double startLat, Double startLng,
                               String endName,   Double endLat,   Double endLng,
                               String mode, double distance, double duration) {
        return MapRouteRes.Leg.builder()
                .startName(startName)
                .startLat(startLat == null ? 0.0 : startLat)
                .startLng(startLng == null ? 0.0 : startLng)
                .endName(endName)
                .endLat(endLat == null ? 0.0 : endLat)
                .endLng(endLng == null ? 0.0 : endLng)
                .mode(normalizeMode(mode))
                .distance(distance)
                .duration(duration)
                .build();
    }

    /**
     * 이동 수단 표준화 (BUS, SUBWAY → TRANSIT)
     * @param raw
     * @return String
     */
    public static String normalizeMode(String raw) {
        if (raw == null) return null;
        return switch (raw.toUpperCase()) {
            case "BUS", "SUBWAY" -> "TRANSIT";
            default -> raw.toUpperCase();
        };
    }

    /**
     * 위치 쌍 목록 생성
     * @param req
     * @return List<int[]>
     */
    public static List<int[]> segmentPairs(MapRouteReq req) {
        var out = new ArrayList<int[]>();
        for (int i = 0; i < req.locations().size() - 1; i++) out.add(new int[]{i, i + 1});
        return out;
    }
}