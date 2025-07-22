package com.gills_dream.global.kakao;

import com.gills_dream.global.kakao.dto.RouteReq;
import com.gills_dream.global.kakao.dto.RouteRes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class KakaoMobilityClient {
    @Value("${kakao.mobility.rest-api-key}")
    private String apiKey;

    RestClient restClient = RestClient.create();

    /**
     * 다중 경유지 경로 탐색 API(자동차)
     * @param routeReq 경로 요청 정보
     * @return 경로 탐색 결과
     */
    public RouteRes findMultiRoute(RouteReq routeReq) {
        return restClient.post()
                .uri("https://apis-navi.kakaomobility.com/v1/waypoints/directions")
                .header("Authorization", "KakaoAK " + apiKey)
                .header("Content-Type", "application/json")
                .body(routeReq)
                .retrieve()
                .body(RouteRes.class);
    }
}
