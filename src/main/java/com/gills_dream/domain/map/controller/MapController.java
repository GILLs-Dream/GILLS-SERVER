package com.gills_dream.domain.map.controller;

import com.gills_dream.domain.map.dto.MapRouteReq;
import com.gills_dream.domain.map.dto.MapSearchRes;
import com.gills_dream.domain.map.service.MapService;
import com.gills_dream.domain.map.service.RouteService;
import com.gills_dream.domain.plan.enumerate.TransportType;
import com.gills_dream.global.map.kakao.KakaoMapClient;
import com.gills_dream.global.map.kakao.KakaoMobilityClient;
import com.gills_dream.global.map.openRouteService.ORSRouteClient;
import com.gills_dream.global.map.tmap.TmapPoiSearchClient;
import com.gills_dream.global.map.tmap.TmapRouteClient;
import com.gills_dream.global.map.tmap.dto.*;
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
    private final TmapPoiSearchClient tmapPoiSearchClient;
    private final MapService mapService;
    private final RouteService routeService;

    @Operation(summary = "경로 탐색 통합", description = "다양한 지도 API를 사용하여 경로 탐색을 통합적으로 처리합니다.")
    @PostMapping("/route")
    public Object unifiedRouteRequest(
            @Parameter(description = "교통수단", example = "CAR") @RequestParam TransportType transportation,
            @RequestBody MapRouteReq routeReq
    ) {
        return routeService.getRoute(transportation, routeReq);
    }

    //Todo: 메소드 연동 후 삭제
    @Operation(summary = "Tmap POI 장소 검색", description = "장소명(키워드)으로 Tmap의 POI 데이터를 검색합니다.")
    @GetMapping("/search/tmap/poi")
    public List<PoiSearchRes.Poi> searchPoiByKeyword(
            @Parameter(description = "검색 키워드", example = "경복궁") @RequestParam String keyword,
            @Parameter(description = "검색 결과 개수", example = "5") @RequestParam(defaultValue = "5") int count
    ) {
        return tmapPoiSearchClient.searchPois(keyword, count);
    }

    @Operation(summary = "KAKAO 키워드 장소 검색", description = "키워드로 KAKAO의 장소 데이터를 검색합니다.")
    @GetMapping("/search/kakao/keyword")
    public List<MapSearchRes>  searchKakaoByKeyword(
            @Parameter(description = "검색 키워드", example = "경복궁") @RequestParam String keyword
    ) {
        return kakaoMapClient.searchByKeyword(keyword);
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
    public List<MapSearchRes> searchTourPlaces(
            @Parameter(description = "검색 키워드", example = "경복궁") @RequestParam String keyword,
            @Parameter(description = "페이지 번호", example = "1") @RequestParam(defaultValue = "1") int pageNo,
            @Parameter(description = "한 페이지당 결과 개수", example = "10") @RequestParam(defaultValue = "10") int numOfRows
    ) {
        return mapService.searchByKeyword(keyword, pageNo, numOfRows);
    }

    @GetMapping("/tour-api/search")
    public List<MapSearchRes> searchTourPlaces(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "12") String category,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int numOfRows
    ) {
        return tourApiClient.searchByKeyword(keyword, category, pageNo, numOfRows);
    }

    @GetMapping("/detail")
    public TourPlaceDetailRes getDetail(
            @RequestParam String contentId
    ) {
        return tourApiClient.getPlaceDetail(contentId);
    }
}
