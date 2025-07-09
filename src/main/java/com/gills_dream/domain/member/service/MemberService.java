package com.gills_dream.domain.member.service;

import com.gills_dream.domain.member.dto.MemberResponseDto;
import com.gills_dream.domain.member.entity.LoginType;
import com.gills_dream.domain.member.entity.Member;
import com.gills_dream.domain.member.entity.RoleType;
import com.gills_dream.global.apiPayload.code.status.SuccessStatus;
import com.gills_dream.global.common.record.TokenPair;

public interface MemberService {
    TokenPair kakaoLogin(String code);
    void kakaoLogout(String accessToken, Member member);
}
