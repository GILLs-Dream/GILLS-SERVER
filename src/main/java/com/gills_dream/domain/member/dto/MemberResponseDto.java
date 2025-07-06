package com.gills_dream.domain.member.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class MemberResponseDto {
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginResultDto {
        @Schema(description = "멤버 id", example = "1")
        private Long memberId;

        @Schema(description = "멤버 이름", example = "길순이")
        private String name;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenDto {
        @Schema(description = "엑세스 토큰", example = "sdfsfsfsfsfdfds")
        @JsonProperty("access_token")
        private String accessToken;

        @Schema(description = "리프레시 토큰", example = "jiljijililjilkl")
        @JsonProperty("refresh_token")
        private String refreshToken;
    }

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

            @JsonProperty("profile")
            private Profile profile;

            @Getter
            @NoArgsConstructor
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class Profile {

                @Schema(description = "닉네임", example = "길순이")
                @JsonProperty("nickname")
                private String nickName;

                @Schema(description = "프로필 사진", example = "http://yyy.kakao.com/dn/.../img_640x640.jpg")
                @JsonProperty("profile_image_url")
                private String profileImg;
            }
        }
    }
}
