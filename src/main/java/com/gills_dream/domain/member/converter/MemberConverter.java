package com.gills_dream.domain.member.converter;

import com.gills_dream.domain.member.dto.MemberResponseDto;
import com.gills_dream.domain.member.entity.LoginType;
import com.gills_dream.domain.member.entity.Member;
import com.gills_dream.domain.member.entity.RoleType;

public class MemberConverter {
    public static MemberResponseDto.LoginResultDto toLoginResultDto(Member member) {
        return MemberResponseDto.LoginResultDto.builder()
                .memberId(member.getMemberId())
                .name(member.getNickName())
                .build();
    }

    public static Member toMember(MemberResponseDto.KakaoUserInfoDto kakaoInfo, RoleType role, LoginType login) {
        MemberResponseDto.KakaoUserInfoDto.KakaoAccount.Profile profile = kakaoInfo.getKakaoAccount().getProfile();
        return Member.builder()
                .nickName(profile.getNickName())
                .profileImg(profile.getProfileImg())
                .email(kakaoInfo.getKakaoAccount().getEmail())
                .loginType(login)
                .roleType(role)
                .build();
    }
}
