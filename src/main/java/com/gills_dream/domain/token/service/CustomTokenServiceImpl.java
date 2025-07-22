package com.gills_dream.domain.token.service;

import com.gills_dream.domain.member.dto.MemberResponseDto;
import com.gills_dream.domain.member.entity.Member;
import com.gills_dream.domain.member.repository.MemberRepository;
import com.gills_dream.global.apiPayload.ApiResponse;
import com.gills_dream.global.apiPayload.code.status.ErrorStatus;
import com.gills_dream.global.apiPayload.exception.GeneralException;
import com.gills_dream.global.common.record.ResponsePair;
import com.gills_dream.global.common.record.TokenPair;
import com.gills_dream.global.jwt.TokenProvider;
import com.gills_dream.global.jwt.TokenUtils;
import com.gills_dream.global.redis.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import org.springframework.http.HttpHeaders;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomTokenServiceImpl implements CustomTokenService {
    private final TokenProvider tokenProvider;
    private final RedisService redisService;
    private final MemberRepository memberRepository;
    private final TokenUtils tokenUtils;

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

    @Override
    public ResponsePair buildLoginResponse(TokenPair tokenPair, HttpHeaders headers) {
        // Token 처리
        tokenUtils.addAccessToken(headers, tokenPair.accessToken());
        tokenUtils.addRefreshToken(headers, tokenPair.refreshToken());

        // Response body 처리
        Member member = tokenPair.member();
        ApiResponse<MemberResponseDto.LoginResultDto> body = ApiResponse.onSuccess(new MemberResponseDto.LoginResultDto(
                                                                                            member.getId(),
                                                                                            member.getEmail()));
        return new ResponsePair(headers, body);
    }
}
