package com.gills_dream.domain.token.dto;

import com.gills_dream.domain.member.entity.Member;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenClass implements Serializable {
    private Long memberId;
    private String accessToken;
    private String refreshToken;
}
