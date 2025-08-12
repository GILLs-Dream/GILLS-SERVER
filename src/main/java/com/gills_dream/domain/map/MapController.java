package com.gills_dream.domain.map;

import com.gills_dream.global.map.kakao.KakaoMapClient;
import com.gills_dream.global.map.kakao.KakaoMobilityClient;
import com.gills_dream.global.map.kakao.dto.KeywordSearchRes;
import com.gills_dream.global.map.kakao.dto.MobilityRouteReq;
import com.gills_dream.global.map.kakao.dto.MobilityRouteRes;
import com.gills_dream.global.map.openRouteService.ORSRouteClient;
import com.gills_dream.global.map.openRouteService.dto.ORSRouteReq;
import com.gills_dream.global.map.openRouteService.dto.ORSRouteRes;
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
import com.gills_dream.global.map.tourapi.TourKeywordSearchRes;

import java.util.List;

@RestController
@RequestMapping("/v1/map")
@RequiredArgsConstructor
@Tag(name = "Map", description = "지도 API")
public class MapController {

    private final TourApiClient tourApiClient;
    private final KakaoMobilityClient kakaoMobilityClient;
    private final KakaoMapClient kakaoMapClient;
    private final TmapRouteClient tmapRouteClient;
    private final TmapPoiSearchClient tmapPoiSearchClient;
    private final ORSRouteClient orsRouteClient;

    //Todo: 메소드 연동 후 삭제
    @Operation(summary = "카카오 다중 경유지 길찾기 요청", description = "카카오 API를 사용하여 다중 경유지 길찾기 요청을 보냅니다.")
    @PostMapping("/route/mobility")
    public MobilityRouteRes kakaoMobilityRouteRequest(@RequestBody MobilityRouteReq mobilityRouteReq) {
        return kakaoMobilityClient.findMultiRoute(mobilityRouteReq);
    }

    //Todo: 메소드 연동 후 삭제
    @Operation(summary = "Tmap 보행자 경로 안내 요청", description = "Tmap API를 사용하여 보행자 경로 안내 요청을 보냅니다.")
    @PostMapping("/route/pedestrian")
    public PedestrianRouteRes tmapPedestrianRouteRequest(@RequestBody PedestrianRouteReq routeReq) {
        return tmapRouteClient.findPedestrianRoute(routeReq);
    }

    //Todo: 메소드 연동 후 삭제
    @Operation(summary = "TMAP 대중교통 경로 요청", description = "TMAP API로 대중교통 경로를 요청합니다.")
    @PostMapping("/route/transit")
    public TransitRawRes tmapTransitRouteRequest(@RequestBody TransitRouteReq req) {
        return tmapRouteClient.findTransitRoute(req);
    }

    //Todo: 메소드 연동 후 삭제
    @Operation(summary = "ORS 자전거 경로 요청", description = "OpenRouteService API를 사용하여 자전거 경로를 요청합니다.")
    @PostMapping("/route/bicycle")
    public ORSRouteRes orsBicycleRouteRequest(@RequestBody ORSRouteReq req) {
        return orsRouteClient.findBicycleRoute(req);
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
    public KeywordSearchRes searchKakaoByKeyword(
            @Parameter(description = "검색 키워드", example = "경복궁") @RequestParam String keyword
    ) {
        return kakaoMapClient.searchByKeyword(keyword);
    }

    @Operation(summary = "KAKAO 주소로 좌표 검색", description = "주소를 입력하여 KAKAO의 좌표 데이터를 검색합니다.")
    @GetMapping("/search/kakao/address")
    public KeywordSearchRes searchKakaoByAddress(
            @Parameter(description = "검색 주소", example = "서울특별시 종로구 세종로 1-68") @RequestParam String address
    ) {
        return kakaoMapClient.searchByAddress(address);
    }

    @GetMapping("/tour-api/search")
    public List<TourKeywordSearchRes> searchTourPlaces(
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
