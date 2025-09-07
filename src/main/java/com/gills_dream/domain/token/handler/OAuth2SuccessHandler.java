package com.gills_dream.domain.token.handler;

import com.gills_dream.domain.member.entity.Member;
import com.gills_dream.domain.member.repository.MemberRepository;
import com.gills_dream.domain.token.dto.TokenClass;
import com.gills_dream.global.apiPayload.code.status.ErrorStatus;
import com.gills_dream.global.apiPayload.exception.GeneralException;
import com.gills_dream.global.jwt.TokenProvider;
import com.gills_dream.global.redis.RedisService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final TokenProvider tokenProvider;
    private final RedisService redisService;
    private final MemberRepository memberRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        try {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal(); // CustomOAuth2Member 객체
            String email = extractEmailFromPrincipal(oAuth2User);
            Member member = memberRepository.findByEmail(email)
                    .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

            // 우리 서버의 jwt 토큰 발행
            String accessToken = tokenProvider.createAccessToken(email);
            String refreshToken = tokenProvider.createRefreshToken(email);
            TokenClass tokenClass = TokenClass.builder()
                    .memberId(member.getId())
                            .accessToken(accessToken)
                    .refreshToken(refreshToken).build();

            // Redis에 일단 tempCode 저장
            String tempCode = UUID.randomUUID().toString();
            redisService.setValue("auth:" + tempCode, tokenClass, 1000 * 60L);

            // tempcode 프론트에 전송
            request.getSession().removeAttribute("SPRING_SECURITY_SAVED_REQUEST");
            String redirectUri = "http://localhost:8080/login/link/kakao?temp=" + tempCode;
            response.sendRedirect(redirectUri);
        } catch (IOException e) {
            throw new GeneralException(ErrorStatus.OAUTH_ERROR);
        }
    }

    private String extractEmailFromPrincipal(OAuth2User user) {
        Map<String, Object> account = (Map<String, Object>) user.getAttributes().get("kakao_account");
        return (String) account.get("email");
    }
}
