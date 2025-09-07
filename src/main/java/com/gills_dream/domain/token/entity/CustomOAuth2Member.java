package com.gills_dream.domain.token.entity;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.Collection;
import java.util.Map;

@Getter
public class CustomOAuth2Member extends DefaultOAuth2User {
    private Long memberId;
    private String email;

    public CustomOAuth2Member(
            Collection<? extends GrantedAuthority> authorities,
            Map<String, Object> attributes,
            String nameAttributeKey,
            String email,
            Long memberId) {
        super(authorities, attributes, nameAttributeKey);
        this.email = email;
        this.memberId = memberId;
    }
}
