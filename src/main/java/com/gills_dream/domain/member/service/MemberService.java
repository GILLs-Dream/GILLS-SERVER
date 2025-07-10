package com.gills_dream.domain.member.service;

import com.gills_dream.domain.member.dto.MemberRequestDto;
import com.gills_dream.domain.member.entity.Member;
import com.gills_dream.global.common.record.TokenPair;

public interface MemberService {
    TokenPair kakaoLogin(String code);
    void kakaoLogout(String accessToken, Member member);
    Member setProfile(Member member, MemberRequestDto.ProfileDto request);
}
