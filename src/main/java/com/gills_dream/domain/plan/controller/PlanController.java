package com.gills_dream.domain.plan.controller;

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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/plan")
@RequiredArgsConstructor
@Tag(name = "Plan", description = "여행계획 API")
public class PlanController {
}
