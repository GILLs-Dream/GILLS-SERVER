package com.gills_dream.domain.place.service;

import com.gills_dream.domain.place.PlaceCache;
import com.gills_dream.domain.place.dto.PlaceSearchReq;
import com.gills_dream.domain.place.dto.PlaceSearchRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceService {
    private final PlaceCacheService placeCacheService;

    public PlaceSearchRes choosePlace(PlaceSearchReq req) {
        return placeCacheService.save(
                String.valueOf(req.platform()),
                req.contentId(),
                req.planId(),
                PlaceCache.builder()
                        .name(req.name())
                        .lat(req.lat())
                        .lng(req.lng())
                        .imageUrl(req.imageUrl())
                        .detailUrl(req.detailUrl())
                        .period(req.period() == null ? null : PlaceCache.Period.builder()
                                .nights(req.period().nights())
                                .dayOrder(req.period().dayOrder())
                                .build())
                        .build()
        );
    }
}
