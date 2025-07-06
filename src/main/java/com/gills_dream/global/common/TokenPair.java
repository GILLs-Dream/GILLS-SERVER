package com.gills_dream.global.common;
import com.gills_dream.domain.member.entity.Member;

public record TokenPair(Member member, String accessToken, String refreshToken) {}
