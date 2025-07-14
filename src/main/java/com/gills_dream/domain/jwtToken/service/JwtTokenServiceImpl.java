package com.gills_dream.domain.jwtToken.service;

import com.gills_dream.domain.member.entity.Member;
import com.gills_dream.domain.member.repository.MemberRepository;
import com.gills_dream.global.apiPayload.code.status.ErrorStatus;
import com.gills_dream.global.apiPayload.exception.GeneralException;
import com.gills_dream.global.common.record.TokenPair;
import com.gills_dream.global.jwt.CustomUserDetails;
import com.gills_dream.global.jwt.TokenProvider;
import com.gills_dream.global.redis.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JwtTokenServiceImpl implements JwtTokenService {
    private final TokenProvider tokenProvider;
    private final RedisService redisService;
    private final MemberRepository memberRepository;

    @Transactional
    @Override
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
}
