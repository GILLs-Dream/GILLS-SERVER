package com.gills_dream.global.map.tourapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TourApiClient {

    @Value("${tour.api.service-key}")
    private String serviceKey;

    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<TourKeywordSearchRes> searchByKeyword(String keyword, String category, int pageNo, int numOfRows) {
        try {
            String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
            String contentTypeId = TourCategory.resolveContentTypeId(category); // 카테고리 ID 조회

            URI uri = UriComponentsBuilder.fromUriString("http://apis.data.go.kr/B551011/KorService2/searchKeyword2")
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("MobileApp", "TourApp")
                    .queryParam("MobileOS", "ETC")
                    .queryParam("pageNo", pageNo)
                    .queryParam("numOfRows", numOfRows)
                    .queryParam("arrange", "C")
                    .queryParam("_type", "json")
                    .queryParam("contentTypeId", contentTypeId)
                    .queryParam("keyword", encodedKeyword)
                    .build(true)
                    .toUri();

            String json = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(String.class);

            return parseJsonResponse(json);

        } catch (Exception e) {
            log.error("Failed to call TourAPI", e);
            return List.of();
        }
    }

    private List<TourKeywordSearchRes> parseJsonResponse(String json) throws Exception {
        List<TourKeywordSearchRes> results = new ArrayList<>();

        JsonNode root = objectMapper.readTree(json);
        JsonNode items = root
                .path("response")
                .path("body")
                .path("items")
                .path("item");

        if (items.isArray()) {
            for (JsonNode node : items) {
                results.add(new TourKeywordSearchRes(
                        node.path("contentid").asText(),
                        node.path("title").asText(),
                        node.path("addr1").asText(),
                        node.path("mapx").asText(),
                        node.path("mapy").asText(),
                        node.path("firstimage").asText()
                ));
            }
        }

        return results;
    }

    public TourPlaceDetailRes getPlaceDetail(String contentId) {
        try {
            URI uri = UriComponentsBuilder.fromUriString("http://apis.data.go.kr/B551011/KorService2/detailCommon2")
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("MobileApp", "TourApp")
                    .queryParam("MobileOS", "IOS")
                    .queryParam("_type", "json")
                    .queryParam("contentId", contentId)
                    .build(true)
                    .toUri();

            String json = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(String.class);

            log.info("TourAPI raw response (detailCommon2):\n{}", json);
            JsonNode root = objectMapper.readTree(json);
            JsonNode item = root
                    .path("response")
                    .path("body")
                    .path("items")
                    .path("item");

            if (item.isArray() && !item.isEmpty()) item = item.get(0);
            if (item.isMissingNode()) return null;

            return new TourPlaceDetailRes(
                    item.path("contentid").asText(),
                    item.path("title").asText(),
                    item.path("overview").asText(),
                    item.path("addr1").asText(),
                    item.path("addr2").asText(),
                    item.path("zipcode").asText(),
                    item.path("mapx").asText(),
                    item.path("mapy").asText(),
                    item.path("cat1").asText(),
                    item.path("cat2").asText(),
                    item.path("cat3").asText(),
                    item.path("sigungucode").asText(),
                    item.path("firstimage").asText()
            );

        } catch (Exception e) {
            log.error("Failed to fetch detail from TourAPI", e);
            return null;
        }
    }
}