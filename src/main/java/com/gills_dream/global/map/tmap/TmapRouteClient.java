package com.gills_dream.global.map.tmap;

import com.gills_dream.global.map.tmap.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Component
@RequiredArgsConstructor
public class TmapRouteClient {

    @Value("${tmap.api.key}")
    private String appKey;

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://apis.openapi.sk.com")
            .build();

    public PedestrianRouteRes findPedestrianRoute(PedestrianRouteReq req) {
        try {
            return restClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/tmap/routes/pedestrian")
                            .queryParam("version", 1)
                            .build())
                    .header("appKey", appKey)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .body(req)
                    .retrieve()
                    .body(PedestrianRouteRes.class);
        } catch (RestClientResponseException e) {
            String body = e.getResponseBodyAsString();
            log.warn("[TMAP][PEDESTRIAN][{}] {}", e.getRawStatusCode(), body);
            throw new RuntimeException(body, e);
        }
    }

    public TransitRawRes findTransitRoute(TransitRouteReq req) {
        try {
            return restClient.post()
                    .uri("/transit/routes")
                    .header("appKey", appKey)
                    .body(req)
                    .retrieve()
                    .body(TransitRawRes.class);
        } catch (RestClientResponseException e) {
            String body = e.getResponseBodyAsString();
            log.warn("[TMAP][TRANSIT][{}] {}", e.getRawStatusCode(), body);
            throw new RuntimeException("대중교통 경로 탐색 실패: " + body, e);
        }
    }
}