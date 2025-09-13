package com.gills_dream.domain.member.service;

import com.gills_dream.domain.member.dto.MemberRequestDto;
import com.gills_dream.domain.member.dto.MemberResponseDto;
import com.gills_dream.domain.member.entity.Member;
import com.gills_dream.global.apiPayload.code.status.SuccessStatus;
import com.gills_dream.global.common.record.TokenPair;
import com.gills_dream.global.jwt.CustomUserDetails;
import org.springframework.web.multipart.MultipartFile;

public interface MemberService {
    void logout(String accessToken, Member member);
    MemberResponseDto.ProfileResultDto setProfile(Member member, MemberRequestDto.ProfileDto request);
    TokenPair kakaoLogin(String accessToken);
    void delete(String accessToken, Member member);
    MemberResponseDto.MemberInfoDto info(CustomUserDetails userDetails);
    TokenPair appleLogin(String identityToken);
}
