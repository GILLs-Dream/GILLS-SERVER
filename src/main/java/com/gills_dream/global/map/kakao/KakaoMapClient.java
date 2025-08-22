package com.gills_dream.global.map.kakao;

import com.gills_dream.domain.map.MapSearchRes;
import com.gills_dream.global.map.kakao.dto.KeywordSearchRes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Component
public class KakaoMapClient {
    @Value("${kakao.map.key}")
    private String apiKey;

    RestClient restClient = RestClient.create();

    /**
     * 장소 키워드 검색 API
     *
     * @param keyword 검색할 키워드
     * @return 검색 결과
     */
    public List<MapSearchRes> searchByKeyword(String keyword) {
        String url = UriComponentsBuilder
                .fromHttpUrl("https://dapi.kakao.com/v2/local/search/keyword.json")
                .queryParam("query", keyword)
                .build()
                .toUriString();

        KeywordSearchRes res = restClient.get()
                .uri(url)
                .header("Authorization", "KakaoAK " + apiKey)
                .retrieve()
                .body(KeywordSearchRes.class);

        if (res == null || res.documents() == null) return List.of();

        return res.documents().stream()
                .map(d -> MapSearchRes.builder()
                        .id(d.id())
                        .name(d.place_name())
                        .category(d.category_name())
                        .lat(d.y())
                        .lng(d.x())
                        .roadAddress(d.road_address_name() != null ? d.road_address_name() : d.address_name())
                        .detailUrl(d.place_url())
                        .imageUrl(null)
                        .build(
                ))
                .toList();
    }

    /**
     * 도로명 주소로 좌표 검색 API
     *
     * @param address 도로명 주소
     * @return 좌표 정보
     */
    public List<MapSearchRes> searchByAddress(String address) {
        String url = UriComponentsBuilder
                .fromHttpUrl("https://dapi.kakao.com/v2/local/search/address.json")
                .queryParam("query", address)
                .build()
                .toUriString();

        KeywordSearchRes res =  restClient.get()
                .uri(url)
                .header("Authorization", "KakaoAK " + apiKey)
                .retrieve()
                .body(KeywordSearchRes.class);
        if (res == null || res.documents() == null) return List.of();

        return res.documents().stream()
                .map(d -> MapSearchRes.builder()
                        .id(d.id())
                        .name(d.place_name())
                        .category(d.category_name())
                        .lat(d.y())
                        .lng(d.x())
                        .roadAddress(d.road_address_name() != null ? d.road_address_name() : d.address_name())
                        .detailUrl(d.place_url())
                        .imageUrl(null)
                        .build(
                        ))
                .toList();
    }
}
