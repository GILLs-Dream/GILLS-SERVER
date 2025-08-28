package com.gills_dream.domain.member.service;

import com.gills_dream.domain.member.dto.MemberResponseDto;
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
    public String getAccessToken(String code) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", kakaoProperties.getClientId());
        body.add("redirect_uri", kakaoProperties.getRedirectUri());
        body.add("code", code);

        MemberResponseDto.TokenDto tokenDto = restClient
                .post()
                .uri(kakaoProperties.getTokenUri())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .body(MemberResponseDto.TokenDto.class);

        return Objects.requireNonNull(tokenDto).accessToken();
    }

    @Override
    public MemberResponseDto.KakaoUserInfoDto getUserInfo(String accessToken) {
        MemberResponseDto.KakaoUserInfoDto userInfoDto = restClient
                .get()
                .uri(kakaoProperties.getUserInfoUri())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .body(MemberResponseDto.KakaoUserInfoDto.class);

        return Objects.requireNonNull(userInfoDto);
    }
}
