package com.gills_dream.domain.member.converter;

import com.gills_dream.domain.member.dto.MemberResponseDto;
import com.gills_dream.domain.member.entity.LoginType;
import com.gills_dream.domain.member.entity.Member;
import com.gills_dream.domain.member.entity.RoleType;
import com.gills_dream.global.common.record.TokenPair;

public class MemberConverter {

    public static MemberResponseDto.TokenDto toTokenDto(TokenPair tokenPair) {
        return new MemberResponseDto.TokenDto(
                tokenPair.accessToken(),
                tokenPair.refreshToken()
        );
    }

    public static MemberResponseDto.LoginResultWithTokenDto toLoginResultWithTokenDto(TokenPair tokenPair) {
        return new MemberResponseDto.LoginResultWithTokenDto(
                tokenPair.member().getId(),
                tokenPair.member().getEmail(),
                tokenPair.accessToken(),
                tokenPair.refreshToken()
        );
    }
}
