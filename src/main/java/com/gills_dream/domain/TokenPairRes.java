// 값만 담는 응답 DTO
package com.gills_dream.domain;

import lombok.Builder;

@Builder
public record TokenPairRes(
        String accessToken,
        String refreshToken
) {
    public static TokenPairRes from(String accessToken, String refreshToken) {
        return TokenPairRes.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

}