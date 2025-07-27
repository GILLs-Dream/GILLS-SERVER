package com.gills_dream.global.map.tmap;

import com.gills_dream.global.map.tmap.dto.PedestrianRouteReq;
import com.gills_dream.global.map.tmap.dto.PedestrianRouteRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;


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
    }
}