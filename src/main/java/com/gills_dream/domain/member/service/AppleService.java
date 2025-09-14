package com.gills_dream.domain.member.service;

import com.gills_dream.domain.member.dto.MemberResponseDto;

public interface AppleService {
    MemberResponseDto.AppleUserInfoDto getUserInfo(String identityToken);
}
