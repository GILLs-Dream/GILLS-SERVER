package com.gills_dream.domain.map.provider;

import com.gills_dream.domain.map.dto.MapRouteReq;
import com.gills_dream.domain.map.dto.MapRouteRes;

import java.util.ArrayList;
import java.util.List;

final class RouteUtils {

    private RouteUtils() {}

    /**
     * 요청 유효성 검사
     * @param req
     * @return boolean
     */
    static boolean invalid(MapRouteReq req) {
        return req == null || req.locations() == null || req.locations().size() < 2;
    }

    /**
     * 빈 경로
     * @return MapRouteRes
     */
    static MapRouteRes empty() {
        return MapRouteRes.builder()
                .distance(0).duration(0).polyline(null).legs(List.of())
                .build();
    }

    /**
     * 경로 응답 생성
     * @param totalDist
     * @param totalTime
     * @param legs
     * @return MapRouteRes
     */
    static MapRouteRes build(double totalDist, double totalTime, List<MapRouteRes.Leg> legs) {
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
    static MapRouteRes.Leg leg(String startName, Double startLat, Double startLng,
                               String endName,   Double endLat,   Double endLng,
                               String mode, double distance, double duration) {
        return MapRouteRes.Leg.builder()
                .startName(startName).startLat(startLat).startLng(startLng)
                .endName(endName).endLat(endLat).endLng(endLng)
                .mode(mode).distance(distance).duration(duration)
                .build();
    }

    /**
     * 위치 쌍 목록 생성
     * @param req
     * @return List<int[]>
     */
    static List<int[]> segmentPairs(MapRouteReq req) {
        var out = new ArrayList<int[]>();
        for (int i = 0; i < req.locations().size() - 1; i++) out.add(new int[]{i, i + 1});
        return out;
    }
}