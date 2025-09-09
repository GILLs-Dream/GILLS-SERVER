package com.gills_dream.domain.member.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class MemberResponseDto {
    public record LoginResultDto(
            @Schema(description = "멤버 id", example = "1")
            Long memberId,

            @Schema(description = "멤버 이메일", example = "gillsoon@gmail.com")
            String email
    ) {}

    public record LoginResultWithTokenDto(
            @Schema(description = "멤버 id", example = "1")
            Long memberId,

            @Schema(description = "멤버 이메일", example = "gillsoon@gmail.com")
            String email,

            @Schema(description = "엑세스 토큰", example = "sdfsfsfsfsfdfds")
            String accessToken,

            @Schema(description = "리프레시 토큰", example = "jiljijililjilkl")
            String refreshToken
    ) {}



    public record ProfileResultDto (
        @Schema(description = "멤버 id", example = "1")
        Long memberId,

        @Schema(description = "멤버 닉네임", example = "길순이")
        String nickname,

        @Schema(description = "정상적으로 업로드되면 활성화될 imageURL", example = "https://influy-s3...")
        String imgUrl
    ) {}


    public record TokenDto (
        @Schema(description = "엑세스 토큰", example = "sdfsfsfsfsfdfds")
        @JsonProperty("access_token")
        String accessToken,

        @Schema(description = "리프레시 토큰", example = "jiljijililjilkl")
        @JsonProperty("refresh_token")
        String refreshToken
    ) {}

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KakaoUserInfoDto {

        @Schema(description = "카카오 계정 id", example = "12343")
        @JsonProperty("id")
        private Long id;

        @JsonProperty("kakao_account")
        private KakaoAccount kakaoAccount;

        @Getter
        @NoArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class KakaoAccount {
            @Schema(description = "이메일", example = "kakao@gmail.com")
            @JsonProperty("email")
            private String email;
        }
    }
}
