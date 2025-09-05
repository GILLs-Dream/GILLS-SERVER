package com.gills_dream.domain.map.service;

import com.gills_dream.domain.map.dto.MapSearchRes;
import com.gills_dream.domain.map.dto.MapSearchResWithPlatform;
import com.gills_dream.domain.place.enumerate.Platform;
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

    /**
     * 키워드로 관광지 검색
     * @param keyword 검색어
     * @return 검색 결과 리스트
     */
    public MapSearchResWithPlatform searchByKeyword(String keyword, int pageNo, int numOfRows) {
        try {
            log.info("Searching for keyword: {}, pageNo: {}, numOfRows: {}", keyword, pageNo, numOfRows);

            // 1. 관광지 API로 검색
            List<MapSearchRes> tour = Optional
                    .ofNullable(tourApiClient.searchByKeyword(keyword, null, pageNo, numOfRows))
                    .orElse(List.of());

            // 2. 관광지 API 결과가 없으면 카카오 API로 검색
            log.info("Tour search result size: {}", tour.size());
            if (!tour.isEmpty()) {
                return MapSearchResWithPlatform.builder()
                        .platform(Platform.TOUR)
                        .mapSearchResList(tour)
                        .build();  // 관광지 API에서 결과가 있으면 반환
            }

            // 3. 관광지 API 결과가 없으면 카카오 API로 검색
            log.info("Tour search returned no results, falling back to Kakao search");

            try {
                List<MapSearchRes> kakao = Optional
                        .ofNullable(kakaoMapClient.searchByKeyword(keyword))
                        .orElse(List.of());
                log.info("Kakao search result size: {}", kakao.size());
                return MapSearchResWithPlatform.builder()
                        .platform(Platform.KAKAO)
                        .mapSearchResList(kakao)
                        .build();  // 카카오 API 결과 반환
            } catch (Exception e) {
                log.warn("Kakao fallback failed: {}", e.getMessage());
                return MapSearchResWithPlatform.builder()
                        .platform(null)
                        .mapSearchResList(List.of())
                        .build();  // 카카오 API도 실패하면 빈 리스트 반환
            }
        } catch (Exception e) {
            log.error("Error searching by keyword: {}, pageNo: {}, numOfRows: {}, error: {}", keyword, pageNo, numOfRows, e.getMessage());
            return MapSearchResWithPlatform.builder()
                    .platform(null)
                    .mapSearchResList(List.of())
                    .build();  // 예외 발생 시 빈 리스트 반환
        }
    }
}
