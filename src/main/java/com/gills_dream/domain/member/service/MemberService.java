package com.gills_dream.domain.member.service;

import com.gills_dream.domain.member.dto.MemberRequestDto;
import com.gills_dream.domain.member.dto.MemberResponseDto;
import com.gills_dream.domain.member.entity.Member;
import com.gills_dream.global.common.record.TokenPair;
import com.gills_dream.global.jwt.CustomUserDetails;

public interface MemberService {
    TokenPair kakaoLogin();
    void kakaoLogout(String accessToken, Member member);
    MemberResponseDto.ProfileResultDto setProfile(Member member, MemberRequestDto.ProfileDto request);
}
