package com.gills_dream.domain.plan.controller;

import com.gills_dream.global.map.kakao.KakaoMobilityClient;
import com.gills_dream.global.map.kakao.dto.MobilityRouteReq;
import com.gills_dream.global.map.kakao.dto.MobilityRouteRes;
import com.gills_dream.global.map.tmap.TmapRouteClient;
import com.gills_dream.global.map.tmap.dto.PedestrianRouteReq;
import com.gills_dream.global.map.tmap.dto.PedestrianRouteRes;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/plan")
@RequiredArgsConstructor
@Tag(name = "Plan", description = "여행계획 API")
public class PlanController {
    private final KakaoMobilityClient kakaoMobilityClient;
    private final TmapRouteClient tmapRouteClient;

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
}
