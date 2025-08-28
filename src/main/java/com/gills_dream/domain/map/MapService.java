package com.gills_dream.domain.map;

import com.gills_dream.domain.place.service.PlaceCacheService;
import com.gills_dream.global.map.kakao.KakaoMapClient;
import com.gills_dream.global.map.tourapi.TourApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MapService {
    private final TourApiClient tourApiClient;
    private final KakaoMapClient kakaoMapClient;
    private final PlaceCacheService placeCacheService;

    /**
     * 키워드로 관광지 검색
     * @param keyword 검색어
     * @return 검색 결과 리스트
     */
    public List<MapSearchRes> searchByKeyword(String keyword, int pageNo, int numOfRows) {
        try {
            log.info("Searching for keyword: {}, pageNo: {}, numOfRows: {}", keyword, pageNo, numOfRows);

            // 1. 관광지 API로 검색
            List<MapSearchRes> tour = Optional
                    .ofNullable(tourApiClient.searchByKeyword(keyword, null, pageNo, numOfRows))
                    .orElse(List.of());

            // 2. 관광지 API 결과가 없으면 카카오 API로 검색
            log.info("Tour search result size: {}", tour.size());
            if (!tour.isEmpty()) {
                return tour;  // 관광지 API에서 결과가 있으면 반환
            }

            // 3. 관광지 API 결과가 없으면 카카오 API로 검색
            log.info("Tour search returned no results, falling back to Kakao search");

            try {
                List<MapSearchRes> kakao = Optional
                        .ofNullable(kakaoMapClient.searchByKeyword(keyword))
                        .orElse(List.of());
                log.info("Kakao search result size: {}", kakao.size());
                return kakao;  // 카카오 API 결과 반환
            } catch (Exception e) {
                log.warn("Kakao fallback failed: {}", e.getMessage());
                return List.of();  // 카카오 API도 실패하면 빈 리스트 반환
            }
        } catch (Exception e) {
            log.error("Error searching by keyword: {}, pageNo: {}, numOfRows: {}, error: {}", keyword, pageNo, numOfRows, e.getMessage());
            return List.of();  // 예외 발생 시 빈 리스트 반환
        }
    }
}
