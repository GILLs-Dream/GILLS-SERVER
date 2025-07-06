package com.gills_dream.domain.member.service;

import com.gills_dream.domain.member.entity.Member;
import com.gills_dream.global.common.TokenPair;

public interface MemberService {
    TokenPair kakaoLogin(String code);
    void kakaoLogout(String accessToken, Member member);
    TokenPair reissue(String refreshToken);

}
