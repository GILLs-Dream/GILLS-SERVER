package com.gills_dream.domain.token.service;

import com.gills_dream.domain.member.entity.LoginType;
import com.gills_dream.domain.member.entity.Member;
import com.gills_dream.domain.member.entity.RoleType;
import com.gills_dream.domain.member.repository.MemberRepository;
import com.gills_dream.domain.token.entity.CustomOAuth2Member;
import com.gills_dream.global.common.properties.AdminProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final AdminProperties adminProperties;
    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId(); // kakao
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email = extractEmail(attributes, provider);

        Member member = memberRepository.findByEmail(email)
                .orElseGet(() -> memberRepository.save(Member.builder()
                        .email(email)
                        .loginType(LoginType.KAKAO)
                        .roleType(decideRole(email))
                        .build()));

        return new CustomOAuth2Member(
                oAuth2User.getAuthorities(),
                attributes,
                userRequest.getClientRegistration()
                        .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName(),
                email,
                member.getId()
        );
    }

    private String extractEmail(Map<String, Object> attributes, String provider) {
        if ("kakao".equals(provider)) {
            Map<String, Object> kakaoAcount = (Map<String, Object>) attributes.get("kakao_account");
            return (String) kakaoAcount.get("email");
        }

        throw new OAuth2AuthenticationException("Unknown privider: " + provider);
    }


    private RoleType decideRole(String email) {
        return adminProperties.getEmails().stream()
                .anyMatch(em -> em.equals(email))
                ? RoleType.ADMIN
                : RoleType.USER;
    }
}
