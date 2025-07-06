package com.gills_dream.domain.member.service;

import com.gills_dream.domain.member.converter.MemberConverter;
import com.gills_dream.domain.member.dto.MemberResponseDto;
import com.gills_dream.domain.member.entity.LoginType;
import com.gills_dream.domain.member.entity.Member;
import com.gills_dream.domain.member.entity.RoleType;
import com.gills_dream.global.apiPayload.code.status.ErrorStatus;
import com.gills_dream.global.apiPayload.exception.GeneralException;
import com.gills_dream.global.common.properties.AdminProperties;
import com.gills_dream.global.common.record.TokenPair;
import com.gills_dream.domain.member.repository.MemberRepository;
import com.gills_dream.global.jwt.TokenProvider;
import com.gills_dream.global.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final KakaoService kakaoService;
    private final AppleService appleService;
    private final TokenProvider tokenProvider;
    private final RedisService redisService;
    private final AdminProperties adminProperties;

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
        redisService.setValue(member.getEmail(), refreshToken, Duration.ofDays(28).toMillis());

        return new TokenPair(member, accessToken, refreshToken);
    }

    @Override
    @Transactional
    public void kakaoLogout(String accessToken, Member member) {
        if (member == null) throw new GeneralException(ErrorStatus.MEMBER_NOT_FOUND);

        // 로그아웃 시 refreshToken을 redis에서 삭제하고 accessToken을 redis에 저장
        redisService.deleteValue(member.getEmail()); // 재로그인 방지
        redisService.setValue(accessToken, "logout", tokenProvider.getExpirationTime(accessToken)); // AccessToken을 블랙리스트에 저장
    }

    @Override
    @Transactional
    public TokenPair reissue(String refreshToken) {
        // ValidateToken false 반환 -> 사용하려는 refreshToken이 유효하지 않음 (redis에 없음)
        if (!tokenProvider.validateToken(refreshToken)) throw new GeneralException(ErrorStatus.INVALID_TOKEN);

        String email = tokenProvider.getUsernameFromToken(refreshToken);
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        String redisRefreshToken = redisService.getValue(member.getEmail());
        if (StringUtils.isEmpty(refreshToken) || StringUtils.isEmpty(redisRefreshToken) || !redisRefreshToken.equals(refreshToken)) {
            throw new GeneralException(ErrorStatus.INVALID_TOKEN);
        }

        return tokenProvider.reissue(member, refreshToken);
    }

    private RoleType decideRole(String email) {
        return adminProperties.getEmails().stream()
                .anyMatch(em -> em.equals(email))
                ? RoleType.ADMIN
                : RoleType.USER;
    }
}
