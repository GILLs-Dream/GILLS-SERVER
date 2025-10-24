package com.gills_dream.domain.member.service;

import com.gills_dream.domain.member.dto.MemberRequestDto;
import com.gills_dream.domain.member.dto.MemberResponseDto;
import com.gills_dream.domain.member.entity.Member;
import com.gills_dream.global.apiPayload.code.status.SuccessStatus;
import com.gills_dream.global.common.record.TokenPair;
import com.gills_dream.global.jwt.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

public interface MemberService {
    void logout(String accessToken, Member member);
    MemberResponseDto.ProfileResultDto setProfile(CustomUserDetails userDetails, MemberRequestDto.ProfileDto request, MultipartFile image);
    MemberResponseDto.TokenPairWithFlagDto kakaoLogin(String accessToken);
    void delete(String accessToken, Member member);
    MemberResponseDto.MemberInfoDto info(CustomUserDetails userDetails);
    MemberResponseDto.TokenPairWithFlagDto appleLogin(String identityToken);
    MemberResponseDto.ProfileResultDto editProfile(CustomUserDetails userDetails, MemberRequestDto.ProfileDto request, MultipartFile image);
    MemberResponseDto.EmailWithTokenDto editEmail(CustomUserDetails userDetails, MemberRequestDto.EmailDto request);
    MemberResponseDto.CheckDto check(String email);
}
