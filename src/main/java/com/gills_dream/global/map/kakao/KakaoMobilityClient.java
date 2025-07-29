package com.gills_dream.global.map.kakao;

import com.gills_dream.global.map.kakao.dto.MobilityRouteReq;
import com.gills_dream.global.map.kakao.dto.MobilityRouteRes;
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
     * @param mobilityRouteReq 경로 요청 정보
     * @return 경로 탐색 결과
     */
    public MobilityRouteRes findMultiRoute(MobilityRouteReq mobilityRouteReq) {
        return restClient.post()
                .uri("https://apis-navi.kakaomobility.com/v1/waypoints/directions")
                .header("Authorization", "KakaoAK " + apiKey)
                .header("Content-Type", "application/json")
                .body(mobilityRouteReq)
                .retrieve()
                .body(MobilityRouteRes.class);
    }
}
