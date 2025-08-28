package com.gills_dream.domain.place.service;

import com.gills_dream.domain.place.vo.PlaceCache;
import com.gills_dream.domain.place.dto.PlaceSearchRes;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PlaceCacheService {

    private final StringRedisTemplate redis;
    private static final Duration TTL = Duration.ofDays(7);

    private String key(String contentId, Long planId) {
        return "plan:" + planId + ":content:" + contentId;
    }

    public PlaceSearchRes save(String platform, String contentId, Long planId, PlaceCache m) {
        String k = key(contentId, planId);
        HashOperations<String, String, String> h = redis.opsForHash();
        h.putAll(k, Map.of(
                "platform", platform,
                "name", nz(m.name()),
                "lat",  String.valueOf(m.lat()),
                "lng",  String.valueOf(m.lng()),
                "imageUrl", nz(m.imageUrl()),
                "detailUrl", nz(m.detailUrl()),
                "placeType", nz(m.placeType()),
                "period.nights", nz(m.period() == null ? null : m.period().nights()),
                "period.dayOrder", nz(m.period() == null ? null : m.period().dayOrder())
        ));
        redis.expire(k, TTL);

        return PlaceSearchRes.builder()
                .contentId(contentId)
                .planId(planId)
                .platform(platform)
                .build();
    }

    public PlaceCache get(String platform, String contentId, Long planId) {
        Map<String, String> m = redis.<String, String>opsForHash().entries(key(contentId, planId));
        if (m == null || m.isEmpty()) return null;
        return PlaceCache.builder()
                .platform(m.getOrDefault("platform", ""))
                .name(m.getOrDefault("name", ""))
                .lat(m.getOrDefault("lat", "0"))
                .lng(m.getOrDefault("lng", "0"))
                .imageUrl(m.getOrDefault("imageUrl", ""))
                .detailUrl(m.getOrDefault("detailUrl", ""))
                .placeType(m.getOrDefault("placeType", ""))
                .period(PlaceCache.Period.builder()
                        .nights(m.getOrDefault("period.nights", ""))
                        .dayOrder(m.getOrDefault("period.dayOrder", ""))
                        .build())
                .build();
    }

    public void delete(String platform, String contentId, Long planId) {
        redis.delete(key(contentId, planId));
    }

    private static String nz(String s){ return s == null ? "" : s; }
    private static double parseDouble(String s){
        try { return s == null ? 0d : Double.parseDouble(s); } catch (Exception e){ return 0d; }
    }
}