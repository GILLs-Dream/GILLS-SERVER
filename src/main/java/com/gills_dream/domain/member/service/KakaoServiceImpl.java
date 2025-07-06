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
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoServiceImpl implements KakaoService {
    private final KakaoProperties kakaoProperties;
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    @Transactional(readOnly = true)
    public String getAccessToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", kakaoProperties.getClientId());
        body.add("redirect_uri", kakaoProperties.getRedirectUri());
        body.add("code", code);

        ResponseEntity<MemberResponseDto.TokenDto> response = restTemplate.postForEntity(
                kakaoProperties.getTokenUri(),
                new HttpEntity<>(body, headers),
                MemberResponseDto.TokenDto.class
        );
        return Objects.requireNonNull(response.getBody()).getAccessToken();
    }

    @Override
    @Transactional(readOnly = true)
    public MemberResponseDto.KakaoUserInfoDto getUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        ResponseEntity<MemberResponseDto.KakaoUserInfoDto> response = restTemplate.exchange(
                kakaoProperties.getUserInfoUri(),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                MemberResponseDto.KakaoUserInfoDto.class
        );

        return Objects.requireNonNull(response.getBody());
    }
}
