package com.gills_dream.domain.token.service;

import com.gills_dream.domain.token.dto.TokenPairResponse;
import com.gills_dream.global.common.record.ResponsePair;
import com.gills_dream.global.common.record.TokenPair;

import org.springframework.http.HttpHeaders;

public interface CustomTokenService {
    TokenPair reissue(String refreshToken);
    ResponsePair buildLoginResponse(TokenPair tokenPair, HttpHeaders headers);
    TokenPairResponse codeToken(String tempCode);
}
