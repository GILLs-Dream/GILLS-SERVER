package com.gills_dream.domain.member.entity;

import lombok.Getter;

@Getter
public enum RoleType {

    USER(Authority.USER),
    ADMIN(Authority.ADMIN);

    private final String authority;

    RoleType(String authority) {
        this.authority = authority;
    }

    public static class Authority {
        public static final String USER = "ROLE_USER";
        public static final String ADMIN = "ROLE_ADMIN";
    }
}
