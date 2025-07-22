package com.gills_dream.domain.member.converter;

import com.gills_dream.domain.member.dto.MemberResponseDto;
import com.gills_dream.domain.member.entity.LoginType;
import com.gills_dream.domain.member.entity.Member;
import com.gills_dream.domain.member.entity.RoleType;

public class MemberConverter {

    public static MemberResponseDto.LoginResultDto toLoginResultDto(Member member) {
        return new MemberResponseDto.LoginResultDto(
                member.getId(),
                member.getEmail()
        );
    }

    public static Member toMember(MemberResponseDto.KakaoUserInfoDto kakaoInfo, RoleType role, LoginType login) {
        return Member.builder()
                .email(kakaoInfo.getKakaoAccount().getEmail())
                .loginType(login)
                .roleType(role)
                .build();
    }
}
