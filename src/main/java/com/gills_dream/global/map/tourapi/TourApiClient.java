package com.gills_dream.global.map.tourapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gills_dream.domain.map.MapSearchRes;
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

    public List<MapSearchRes> searchByKeyword(String keyword, String category, int pageNo, int numOfRows) {
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
    }

    private List<MapSearchRes> parseJsonResponse(String json) {
        try {
            if (json == null || json.isBlank()) return List.of();

            String t = json.trim();
            if (t.startsWith("[") && t.endsWith("]")) return List.of();

            JsonNode root = objectMapper.readTree(json);

            // items 안전 접근
            JsonNode itemsNode = root.path("response").path("body").path("items");
            boolean noItems =
                    itemsNode.isMissingNode() || itemsNode.isNull() ||
                            (itemsNode.isTextual() && itemsNode.asText().trim().isEmpty()) ||
                            (itemsNode.isObject() && itemsNode.size() == 0) ||
                            (itemsNode.isArray() && itemsNode.size() == 0);
            if (noItems) {
                log.info("TourAPI: items empty");
                return null;
            }

            JsonNode item = itemsNode.path("item");
            if (item.isMissingNode()) return List.of();
            if (!item.isArray()) item = objectMapper.readTree("[" + item.toString() + "]");

            List<MapSearchRes> out = new ArrayList<>();
            for (JsonNode n : item) {
                String id    = n.path("contentid").asText(null);
                String name  = n.path("title").asText(null);
                String addr1 = n.path("addr1").asText(null);
                String mapx  = n.path("mapx").asText(null); // 경도
                String mapy  = n.path("mapy").asText(null); // 위도
                String img   = n.path("firstimage").asText(null);
                String type  = n.path("contenttypeid").asText(null);

                String category = null;
                var tc = TourCategory.fromContentTypeId(type);
                if (tc != null) category = tc.toLowerCase();

                out.add(MapSearchRes.builder()
                        .id(id).name(name).category(category)
                        .lat(mapy).lng(mapx)
                        .roadAddress(addr1).detailUrl(null)
                        .imageUrl((img == null || img.isBlank()) ? null : img)
                        .build());
            }
            return out;

        } catch (Exception e) {
            log.error("parseJsonResponse failed", e);
            return List.of();
        }
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