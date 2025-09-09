package com.gills_dream.domain.member.service;

import com.gills_dream.domain.member.dto.MemberResponseDto;
import com.gills_dream.global.apiPayload.code.status.ErrorStatus;
import com.gills_dream.global.apiPayload.exception.GeneralException;
import com.gills_dream.global.common.properties.KakaoProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KakaoServiceImpl implements KakaoService {
    private final KakaoProperties kakaoProperties;
    private final RestClient restClient = RestClient.create();

    @Override
    public MemberResponseDto.KakaoUserInfoDto getUserInfo(String accessToken) {
        MemberResponseDto.KakaoUserInfoDto userInfoDto = restClient
                .get()
                .uri(kakaoProperties.getUserInfoUri())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .body(MemberResponseDto.KakaoUserInfoDto.class);

        if (userInfoDto == null) throw new GeneralException(ErrorStatus.CANNOT_LOAD_USER_FROM_KAKAO);

        return Objects.requireNonNull(userInfoDto);
    }
}
