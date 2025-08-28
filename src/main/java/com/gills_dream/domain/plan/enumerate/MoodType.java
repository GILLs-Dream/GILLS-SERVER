package com.gills_dream.domain.plan.enumerate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MoodType {
    HEALING("힐링"),
    ROMANTIC("로맨틱"),
    TROPICAL("트로피컬"),
    CROWDED("시끌벅적"),
    INSTAGRAM_VIBE("인스타 감성"),
    LUXURY("럭셔리"),
    EXTREME("익스트림"),
    VINTAGE("빈티지"),
    NATURE("자연"),
    LOCAL_VIBE("현지 감성"),
    EXOTIC("이국적"),
    CULTURE_EXPERIENCE("문화 체험"),
    FAMILY_TRIP("가족 여행"),
    FRIENDS_TRIP("친구와 여행"),
    ALONE_TRIP("혼자 여행"),
    COUPLE_TRIP("커플 여행"),
    FREEDOM("자유"),
    PEACEFUL("고즈넉");
    private final String description;
}