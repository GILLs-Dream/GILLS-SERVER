package com.gills_dream.domain.token.dto;

public record TokenPairResponse (
        Long memberId,
        String accessToken,
        String refreshToken
){}
