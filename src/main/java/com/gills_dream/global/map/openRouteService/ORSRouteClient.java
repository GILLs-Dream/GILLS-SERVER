package com.gills_dream.global.map.openRouteService;

import com.gills_dream.global.map.openRouteService.dto.ORSRouteReq;
import com.gills_dream.global.map.openRouteService.dto.ORSRouteRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ORSRouteClient {

    @Value("${ors.api.key}")
    private String apiKey;

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://api.openrouteservice.org")
            .build();

    public ORSRouteRes findBicycleRoute(ORSRouteReq request) {
        try {
            return restClient.post()
                    .uri("/v2/directions/cycling-regular/geojson")
                    .header("Authorization", apiKey)
                    .header("Content-Type", "application/json")
                    .body(Map.of(
                            "coordinates", List.of(
                                    List.of(request.startLon(), request.startLat()),
                                    List.of(request.endLon(), request.endLat())
                            )
                    ))
                    .retrieve()
                    .body(ORSRouteRes.class);
        } catch (RestClientResponseException e) {
            log.error("자전거 경로 탐색 실패: {}", e.getResponseBodyAsString(), e);
            throw new RuntimeException("OpenRouteService 자전거 경로 요청 실패", e);
        }
    }
}