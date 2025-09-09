package com.gills_dream.domain.member.service;

import com.gills_dream.domain.member.dto.MemberRequestDto;
import com.gills_dream.domain.member.dto.MemberResponseDto;
import com.gills_dream.domain.member.entity.Member;
import com.gills_dream.global.common.record.TokenPair;

public interface MemberService {
    void kakaoLogout(String accessToken, Member member);
    MemberResponseDto.ProfileResultDto setProfile(Member member, MemberRequestDto.ProfileDto request);
    TokenPair kakaoLoginWithToken(String accessToken);
}
