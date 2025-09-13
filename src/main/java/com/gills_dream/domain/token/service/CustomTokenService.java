package com.gills_dream.domain.token.service;

import com.gills_dream.global.common.record.TokenPair;

public interface CustomTokenService {
    TokenPair reissue(String refreshToken);
}
