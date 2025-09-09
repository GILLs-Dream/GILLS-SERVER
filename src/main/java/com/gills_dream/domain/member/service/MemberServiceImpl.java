package com.gills_dream.domain.member.service;

import com.gills_dream.domain.member.dto.MemberRequestDto;
import com.gills_dream.domain.member.dto.MemberResponseDto;
import com.gills_dream.domain.member.entity.LoginType;
import com.gills_dream.domain.member.entity.Member;
import com.gills_dream.domain.member.entity.RoleType;
import com.gills_dream.domain.plan.entity.Plan;
import com.gills_dream.domain.plan.repository.PlanRepository;
import com.gills_dream.global.apiPayload.code.status.ErrorStatus;
import com.gills_dream.global.apiPayload.exception.GeneralException;
import com.gills_dream.global.common.properties.AdminProperties;
import com.gills_dream.domain.member.repository.MemberRepository;
import com.gills_dream.global.common.record.TokenPair;
import com.gills_dream.global.jwt.TokenProvider;
import com.gills_dream.global.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final KakaoService kakaoService;
    private final AppleService appleService;
    private final TokenProvider tokenProvider;
    private final RedisService redisService;
    private final AdminProperties adminProperties;
    private final PlanRepository planRepository;

    @Override
    @Transactional
    public TokenPair kakaoLogin(String kakaoAccessToken) {
        // 유저에 대한 정보 받기
        MemberResponseDto.KakaoUserInfoDto kakaoUser = kakaoService.getUserInfo(kakaoAccessToken);

        // 우리 서버의 jwt 토큰 발행
        String email = kakaoUser.getKakaoAccount().getEmail();
        if (email == null) throw new GeneralException(ErrorStatus.EMAIL_IS_REQUIRED);

        Member member = memberRepository.findByEmail(email)
            .orElseGet(() -> memberRepository.save(Member.builder()
                    .email(kakaoUser.getKakaoAccount().getEmail())
                    .loginType(LoginType.KAKAO)
                    .roleType(decideRole(email))
                    .build()));

        String accessToken = tokenProvider.createAccessToken(member.getEmail());
        String refreshToken = tokenProvider.createRefreshToken(member.getEmail());

        // Redis에 refresh token 저장
        redisService.setValue(member.getEmail(), refreshToken, Duration.ofDays(28).toMillis());

        return new TokenPair(member, accessToken, refreshToken);
    }

    @Override
    @Transactional
    public TokenPair appleLogin(String identityToken) {
        MemberResponseDto.AppleUserInfoDto appleUser = appleService.getUserInfo(identityToken);

        String email = appleUser.getEmail();
        if (email == null) throw new GeneralException(ErrorStatus.EMAIL_IS_REQUIRED);

        Member member = memberRepository.findByEmail(email)
                .orElseGet(() -> memberRepository.save(Member.builder()
                        .email(email)
                        .loginType(LoginType.APPLE)
                        .roleType(decideRole(email))
                        .build()));

        String accessToken = tokenProvider.createAccessToken(member.getEmail());
        String refreshToken = tokenProvider.createRefreshToken(member.getEmail());

        redisService.setValue(member.getEmail(), refreshToken, Duration.ofDays(28).toMillis());

        return new TokenPair(member, accessToken, refreshToken);
    }


    @Override
    @Transactional
    public void delete(String accessToken, Member memberDet) {
        if (memberDet == null) throw new GeneralException(ErrorStatus.MEMBER_NOT_FOUND);

        Member member = memberRepository.findById(memberDet.getId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        // refreshToken을 redis에서 삭제하고 accessToken을 redis에 저장
        redisService.deleteValue(member.getEmail()); // 재로그인 방지
        redisService.setValue(accessToken, "logout", tokenProvider.getExpirationTime(accessToken)); // AccessToken을 블랙리스트에 저장

        // DB에서 멤버 삭제
        for (Plan plan : member.getPlanList()) {
            plan.updateMember(null);
        }
        planRepository.flush();
        member.getPlanList().clear();
        memberRepository.delete(member);
    }

    @Override
    @Transactional
    public void logout(String accessToken, Member member) {
        if (member == null) throw new GeneralException(ErrorStatus.MEMBER_NOT_FOUND);

        // 로그아웃 시 refreshToken을 redis에서 삭제하고 accessToken을 redis에 저장
        redisService.deleteValue(member.getEmail()); // 재로그인 방지
        redisService.setValue(accessToken, "logout", tokenProvider.getExpirationTime(accessToken)); // AccessToken을 블랙리스트에 저장
    }


    @Override
    @Transactional
    public MemberResponseDto.ProfileResultDto setProfile(Member member, MemberRequestDto.ProfileDto request) {
        if (!Objects.equals(member.getId(), request.memberId())) throw new GeneralException(ErrorStatus.MEMBER_NOT_FOUND);

        if (request.nickname() == null) throw new GeneralException(ErrorStatus.NICKNAME_IS_NECESSARY);
        member.updateNickname(request.nickname());
        if (request.profileImg() != null) member.updateProfileImg(request.profileImg());
        if (request.marketingAgreement() != null) member.updateMarketingAgreement(request.marketingAgreement());

        memberRepository.save(member);
        return new MemberResponseDto.ProfileResultDto(
                member.getId(),
                member.getNickname()
        );
    }

    private RoleType decideRole(String email) {
        return adminProperties.getEmails().stream()
                .anyMatch(em -> em.equals(email))
                ? RoleType.ADMIN
                : RoleType.USER;
    }
}
