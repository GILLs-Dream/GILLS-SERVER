package com.gills_dream.global.common.record;

import com.gills_dream.domain.member.dto.MemberResponseDto;
import com.gills_dream.global.apiPayload.ApiResponse;
import org.springframework.http.HttpHeaders;


public record ResponsePair(HttpHeaders headers, ApiResponse<MemberResponseDto.LoginResultDto> body) {}
