package com.gills_dream.domain.member.service;

import com.gills_dream.domain.member.dto.MemberResponseDto;

public interface KakaoService {
//    String getAccessToken(String code);
    MemberResponseDto.KakaoUserInfoDto getUserInfo(String accessToken);
}
