package com.gills_dream.domain.map.controller;

import com.gills_dream.domain.map.dto.MapRouteReq;
import com.gills_dream.domain.map.dto.MapSearchRes;
import com.gills_dream.domain.map.dto.MapSearchResWithPlatform;
import com.gills_dream.domain.map.service.MapService;
import com.gills_dream.domain.map.service.RouteService;
import com.gills_dream.domain.plan.enumerate.TransportType;
import com.gills_dream.global.map.kakao.KakaoMapClient;
import com.gills_dream.global.map.tmap.TmapPoiSearchClient;
import com.gills_dream.global.map.tourapi.TourApiClient;
import com.gills_dream.global.map.tourapi.TourPlaceDetailRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/v1/map")
@RequiredArgsConstructor
@Tag(name = "Map", description = "지도 API")
public class MapController {

    private final TourApiClient tourApiClient;
    private final KakaoMapClient kakaoMapClient;
    private final MapService mapService;
    private final RouteService routeService;

    @Operation(summary = "경로탐색 요청", description = "출발지와 도착지 간의 경로를 탐색합니다.")
    @GetMapping("/route")
    public Object requestRoute(
            @Parameter(description = "플랜아이디", example = "1") @RequestParam(required = false) Long planId,
            @Parameter(description = "교통수단", example = "CAR") @RequestParam TransportType transportation
    ) {
        return routeService.getRoute(planId, transportation);
    }

    @Operation(summary = "KAKAO 키워드 장소 검색", description = "키워드로 KAKAO의 장소 데이터를 검색합니다.")
    @GetMapping("/search/kakao/keyword")
    public List<MapSearchRes>  searchKakaoByKeyword(
            @Parameter(description = "검색 키워드", example = "경복궁") @RequestParam String keyword
    ) {
        return kakaoMapClient.searchByKeyword(keyword);
    }

    @Operation(summary = "KAKAO 장소 상세정보", description = "KAKAO 장소 ID로 장소의 상세 정보를 조회합니다.")
    @GetMapping("/search/kakao/detail")
    public String searchKakaoPlaceDetail(
            @Parameter(description = "KAKAO 장소명", example = "데코아발림") @RequestParam String kakaoPlaceName,
            @Parameter(description = "KAKAO 장소 카테고리", example = "카페") @RequestParam String kakaoPlaceCategory,
            @Parameter(description = "KAKAO 장소 주소", example = "서울 마포구 독막로15길 13-6") @RequestParam(required = false) String address
    ) {
        return mapService.getKakaoPlaceDetail(kakaoPlaceName, kakaoPlaceCategory, address);
    }

    @Operation(summary = "KAKAO 주소로 좌표 검색", description = "주소를 입력하여 KAKAO의 좌표 데이터를 검색합니다.")
    @GetMapping("/search/kakao/address")
    public List<MapSearchRes> searchKakaoByAddress(
            @Parameter(description = "검색 주소", example = "서울특별시 종로구 세종로 1-68") @RequestParam String address
    ) {
        return kakaoMapClient.searchByAddress(address);
    }
    @Operation(
            summary = "관광지 검색",
            description = "Tour API, KAKAO API를 사용하여 장소를 검색합니다. "
                    + "planId가 없으면 일반 검색, 있으면 해당 플랜과 연동된 검색 결과를 캐싱합니다."
    )
    @GetMapping("/search/place")
    public MapSearchResWithPlatform searchTourPlaces(
            @Parameter(description = "검색 키워드", example = "경복궁") @RequestParam String keyword,
            @Parameter(description = "페이지 번호", example = "1") @RequestParam(defaultValue = "1") int pageNo,
            @Parameter(description = "한 페이지당 결과 개수", example = "10") @RequestParam(defaultValue = "10") int numOfRows
    ) {
        return mapService.searchByKeyword(keyword, pageNo, numOfRows);
    }

    @GetMapping("/detail")
    public TourPlaceDetailRes getDetail(
            @RequestParam String contentId
    ) {
        return tourApiClient.getPlaceDetail(contentId);
    }

    /*
    @Operation(summary = "경로 탐색 통합", description = "다양한 지도 API를 사용하여 경로 탐색을 통합적으로 처리합니다.")
    @PostMapping("/route")
    public Object unifiedRouteRequest(
            @Parameter(description = "교통수단", example = "CAR") @RequestParam TransportType transportation,
            @RequestBody MapRouteReq routeReq
    ) {
        return routeService.getRoute(transportation, routeReq);
    }

    @Operation(summary = "Tmap POI 장소 검색", description = "장소명(키워드)으로 Tmap의 POI 데이터를 검색합니다.")
    @GetMapping("/search/tmap/poi")
    public List<PoiSearchRes.Poi> searchPoiByKeyword(
            @Parameter(description = "검색 키워드", example = "경복궁") @RequestParam String keyword,
            @Parameter(description = "검색 결과 개수", example = "5") @RequestParam(defaultValue = "5") int count
    ) {
        return tmapPoiSearchClient.searchPois(keyword, count);
    }*/
}
