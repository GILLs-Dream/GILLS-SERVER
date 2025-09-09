package com.gills_dream.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class MemberRequestDto {
    public record ProfileDto (
            @Schema(description = "멤버 id", example = "1")
            Long memberId,

            @Schema(description = "멤버 닉네임", example = "길순이")
            String nickname,

            @Schema(description = "마켓팅 수신 동의 여부", example = "true")
            Boolean marketingAgreement
    ) {}
}
