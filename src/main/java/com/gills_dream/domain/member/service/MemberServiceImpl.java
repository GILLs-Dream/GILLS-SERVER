package com.gills_dream.domain.member.service;

import com.gills_dream.domain.member.converter.MemberConverter;
import com.gills_dream.domain.member.dto.MemberResponseDto;
import com.gills_dream.domain.member.entity.LoginType;
import com.gills_dream.domain.member.entity.Member;
import com.gills_dream.domain.member.entity.RoleType;
import com.gills_dream.global.common.properties.AdminProperties;
import com.gills_dream.global.common.TokenPair;
import com.gills_dream.domain.member.repository.MemberRepository;
import com.gills_dream.global.jwt.TokenProvider;
import com.gills_dream.global.redis.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final KakaoService kakaoService;
    private final AppleService appleService;
    private final TokenProvider tokenProvider;
    private final RedisService redisService;
    private final AdminProperties adminProperties;
    private static final long REFRESH_TOKEN_VALIDITY_SECONDS = 24 * 60 * 60 * 7 * 4; // refresh token: 4주

    @Override
    @Transactional
    public TokenPair kakaoLogin(String code) {
        // 카카오에 엑세스 토큰 요청
        String kakaoAccessToken = kakaoService.getAccessToken(code);

        // 유저에 대한 정보 받기
        MemberResponseDto.KakaoUserInfoDto kakaoUser = kakaoService.getUserInfo(kakaoAccessToken);

        // 우리 서버의 jwt 토큰 발행
        String email = kakaoUser.getKakaoAccount().getEmail();
        Member member = memberRepository.findByEmail(email)
                .orElseGet(() -> memberRepository.save(MemberConverter.toMember(kakaoUser, decideRole(email), LoginType.KAKAO)));

        String accessToken = tokenProvider.createAccessToken(member.getEmail());
        String refreshToken = tokenProvider.createRefreshToken(member.getEmail());

        // Redis에 refresh token 저장
        redisService.setValue(member.getEmail(), refreshToken, 1000 * REFRESH_TOKEN_VALIDITY_SECONDS);

        return new TokenPair(member, accessToken, refreshToken);
    }

    private RoleType decideRole(String email) {
        return adminProperties.getEmails().stream()
                .anyMatch(em -> em.equals(email))
                ? RoleType.ADMIN
                : RoleType.USER;
    }
}
