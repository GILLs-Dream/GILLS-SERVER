package com.gills_dream.global.map.tmap;

import com.gills_dream.global.map.tmap.dto.PoiSearchRes;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TmapPoiSearchClient {

    @Value("${tmap.api.key}")
    private String appKey;

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://apis.openapi.sk.com")
            .defaultHeader("Accept", "application/json")
            .defaultHeader("Content-Type", "application/json")
            .build();

    public List<PoiSearchRes.Poi> searchPois(String keyword, int count) {
        String uri = "/tmap/pois?version=1&format=json"
                + "&searchKeyword=" + keyword
                + "&resCoordType=WGS84GEO"
                + "&reqCoordType=WGS84GEO"
                + "&count=" + count;

        PoiSearchRes response = restClient.get()
                .uri(uri)
                .header("appKey", appKey)
                .retrieve()
                .body(PoiSearchRes.class);

        if (response != null && response.searchPoiInfo() != null &&
                response.searchPoiInfo().pois() != null &&
                response.searchPoiInfo().pois().poi() != null) {
            return response.searchPoiInfo().pois().poi();
        }

        return List.of();
    }
}