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

        h.putAll(hashKey, Map.of(
                "platform", platform,
                "name", nz(m.name()),
                "lat",  String.valueOf(m.lat()),
                "lng",  String.valueOf(m.lng()),
                "imageUrl", nz(m.imageUrl()),
                "detailUrl", nz(m.detailUrl()),
                "placeType", nz(m.placeType()),
                "period.nights", nz(m.period() == null ? "" : m.period().nights()),
                "period.dayOrder", nz(m.period() == null ? "" : m.period().dayOrder())
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

            result.add(PlaceCache.builder()
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
                    .build());
        }
        return result;
    }

    /** 플랜 + 콘텐츠 단건 조회 */
    public PlaceCache getOne(Long planId, String contentId) {
        Map<String, String> m = redis.<String, String>opsForHash().entries(contentKey(planId, contentId));
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

    private static String nz(String s){ return s == null ? "" : s; }
}