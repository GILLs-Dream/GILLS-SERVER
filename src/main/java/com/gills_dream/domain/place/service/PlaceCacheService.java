package com.gills_dream.domain.place.service;

import com.gills_dream.domain.place.vo.PlaceCache;
import com.gills_dream.domain.place.dto.PlaceSearchRes;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PlaceCacheService {

    private final StringRedisTemplate redis;
    private static final Duration TTL = Duration.ofDays(7);

    private String planKey(Long planId) {
        return "plan:" + planId;
    }
    private String contentKey(Long planId, String contentId) {
        return planKey(planId) + ":content:" + contentId;
    }
    private String indexKey(Long planId) {
        return planKey(planId) + ":contents";
    }

    /** 저장 */
    public PlaceSearchRes save(String platform, String contentId, Long planId, PlaceCache m) {
        String hashKey = contentKey(planId, contentId);
        HashOperations<String, String, String> h = redis.opsForHash();

        h.putAll(hashKey, Map.ofEntries(
                Map.entry("platform", platform),
                Map.entry("name", nz(m.name())),
                Map.entry("lat", String.valueOf(m.lat())),
                Map.entry("lng", String.valueOf(m.lng())),
                Map.entry("imageUrl", nz(m.imageUrl())),
                Map.entry("detailUrl", nz(m.detailUrl())),
                Map.entry("placeType", nz(String.valueOf(m.placeType()))),
                Map.entry("period.startedAt", nz(m.period() == null ? "" : String.valueOf(m.period().startedAt()))),
                Map.entry("period.finishedAt", nz(m.period() == null ? "" : String.valueOf(m.period().finishedAt()))),
                Map.entry("period.duration", m.period() == null || m.period().duration() == null ? "" : String.valueOf(m.period().duration())),
                Map.entry("period.dayOrder", m.period() == null || m.period().dayOrder() == null ? "" : String.valueOf(m.period().dayOrder()))
        ));

        // TTL
        redis.expire(hashKey, TTL);

        String setKey = indexKey(planId);
        redis.opsForSet().add(setKey, contentId);
        redis.expire(setKey, TTL);

        return PlaceSearchRes.builder()
                .contentId(contentId)
                .planId(planId)
                .platform(platform)
                .build();
    }

    /** 플랜 기준 전체 조회 */
    public List<PlaceCache> getByPlanId(Long planId) {
        String setKey = indexKey(planId);
        Set<String> contentIds = redis.opsForSet().members(setKey);
        if (contentIds == null || contentIds.isEmpty()) return List.of();

        List<PlaceCache> result = new ArrayList<>(contentIds.size());
        HashOperations<String, String, String> h = redis.opsForHash();

        for (String contentId : contentIds) {
            Map<String, String> m = h.entries(contentKey(planId, contentId));
            if (m == null || m.isEmpty()) continue;

            PlaceCache.Period period = PlaceCache.Period.builder()
                    .startedAt(m.getOrDefault("period.startedAt", ""))
                    .finishedAt(m.getOrDefault("period.finishedAt", ""))
                    .duration(parseNullableInt(m.get("period.duration")))     // << 안전 파싱
                    .dayOrder(parseNullableIntOrDefault(m.get("period.dayOrder"), 0))
                    .build();

            result.add(PlaceCache.builder()
                    .contentId(contentId)
                    .platform(m.getOrDefault("platform", ""))
                    .name(m.getOrDefault("name", ""))
                    .lat(m.getOrDefault("lat", "0"))
                    .lng(m.getOrDefault("lng", "0"))
                    .imageUrl(m.getOrDefault("imageUrl", ""))
                    .detailUrl(m.getOrDefault("detailUrl", ""))
                    .placeType(m.getOrDefault("placeType", ""))
                    .period(period)
                    .build());
        }
        return result;
    }

    /** 플랜 + 콘텐츠 단건 조회 */
    public PlaceCache getOne(Long planId, String contentId) {
        Map<String, String> m = redis.<String, String>opsForHash().entries(contentKey(planId, contentId));
        if (m == null || m.isEmpty()) return null;

        PlaceCache.Period period = PlaceCache.Period.builder()
                .startedAt(m.getOrDefault("period.startedAt", ""))
                .finishedAt(m.getOrDefault("period.finishedAt", ""))
                .duration(parseNullableInt(m.get("period.duration")))
                .dayOrder(parseNullableIntOrDefault(m.get("period.dayOrder"), 0))
                .build();

        return PlaceCache.builder()
                .contentId(contentId)
                .platform(m.getOrDefault("platform", ""))
                .name(m.getOrDefault("name", ""))
                .lat(m.getOrDefault("lat", "0"))
                .lng(m.getOrDefault("lng", "0"))
                .imageUrl(m.getOrDefault("imageUrl", ""))
                .detailUrl(m.getOrDefault("detailUrl", ""))
                .placeType(m.getOrDefault("placeType", ""))
                .period(period)
                .build();
    }

    /** 특정 장소만 삭제 */
    public void deleteOne(Long planId, String contentId) {
        redis.delete(contentKey(planId, contentId));
        redis.opsForSet().remove(indexKey(planId), contentId);
    }

    /** 플랜 전체 삭제 */
    public void deleteAll(Long planId) {
        String setKey = indexKey(planId);
        Set<String> contentIds = redis.opsForSet().members(setKey);
        if (contentIds != null) {
            for (String cid : contentIds) {
                redis.delete(contentKey(planId, cid));
            }
        }
        redis.delete(setKey);
    }

    private static Integer parseNullableInt(String s) {
        if (s == null) return null;
        String t = s.trim();
        if (t.isEmpty() || t.equalsIgnoreCase("null")) return null;
        try { return Integer.valueOf(t); }
        catch (NumberFormatException e) { return null; } // 쓰레기값은 null 처리
    }

    private static Integer parseNullableIntOrDefault(String s, int defaultVal) {
        Integer v = parseNullableInt(s);
        return v == null ? defaultVal : v;
    }

    private static String nz(String s){ return s == null ? "" : s; }
}