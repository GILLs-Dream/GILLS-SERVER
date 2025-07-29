package com.gills_dream.global.map.tourapi;

public record TourPlaceDetailRes(
        String contentId,
        String title,
        String overview,
        String addr1,
        String addr2,
        String zipcode,
        String mapx,
        String mapy,
        String cat1,
        String cat2,
        String cat3,
        String sigunguCode,
        String firstimage
) {
}