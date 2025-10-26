package com.gills_dream.domain.map.service;

import com.gills_dream.domain.gpt.service.GptService;
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
    private final GptService gptService;

    /**
     * 키워드로 관광지 검색
     * @param keyword 검색어
     * @return 검색 결과 리스트
     */
    public MapSearchResWithPlatform searchByKeyword(String keyword, int pageNo, int numOfRows) {
        log.info("Searching for keyword: {}, pageNo: {}, numOfRows: {}", keyword, pageNo, numOfRows);

        // 1. 관광지 API 시도
        List<MapSearchRes> tour;
        try {
            tour = Optional
                    .ofNullable(tourApiClient.searchByKeyword(keyword, null, pageNo, numOfRows))
                    .orElse(List.of());
            log.info("Tour search result size: {}", tour.size());
        } catch (Exception te) {
            // 관광공사 API가 500 등으로 터져도 여기서 먹고 감싸줌
            log.warn("Tour API failed for keyword {}: {}", keyword, te.getMessage());
            tour = List.of();
        }

        // 2. 관광지 API에 결과가 있으면 그대로 리턴
        if (!tour.isEmpty()) {
            return MapSearchResWithPlatform.builder()
                    .platform(Platform.TOUR)
                    .mapSearchResList(tour)
                    .build();
        }

        // 3. 관광지 API 결과 없거나 실패 -> 카카오 API fallback
        log.info("Tour search returned no results, falling back to Kakao search");

        try {
            List<MapSearchRes> kakao = Optional
                    .ofNullable(kakaoMapClient.searchByKeyword(keyword))
                    .orElse(List.of());
            log.info("Kakao search result size: {}", kakao.size());

            return MapSearchResWithPlatform.builder()
                    .platform(Platform.KAKAO)
                    .mapSearchResList(kakao)
                    .build();
        } catch (Exception ke) {
            // 카카오도 실패하면 최종 빈 결과
            log.warn("Kakao fallback failed for keyword {}: {}", keyword, ke.getMessage());
            return MapSearchResWithPlatform.builder()
                    .platform(null)
                    .mapSearchResList(List.of())
                    .build();
        }
    }

    /**
     * 카카오 장소 ID로 장소 상세정보 조회
     * @param kakaoPlaceName 카카오 장소명
     * @param kakaoPlaceCategory 카카오 장소 카테고리
     * @return 장소 상세정보
     */
    public String getKakaoPlaceDetail(String kakaoPlaceName, String kakaoPlaceCategory, String address) {
        try {
            log.info("Fetching Kakao place detail for Name: {}", kakaoPlaceName, kakaoPlaceCategory);
            return gptService.getKakaoPlaceDetail(kakaoPlaceName, kakaoPlaceCategory, address);
        } catch (Exception e) {
            log.error("Error fetching Kakao place detail for Name: {}, error: {}", kakaoPlaceName, e.getMessage());
            return null;  // 예외 발생 시 null 반환
        }
    }
}
