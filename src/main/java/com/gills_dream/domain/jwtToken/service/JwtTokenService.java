package com.gills_dream.domain.jwtToken.service;

import com.gills_dream.global.common.record.TokenPair;
import com.gills_dream.global.jwt.CustomUserDetails;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;

public interface JwtTokenService {
    TokenPair reissue(String refreshToken);
}
