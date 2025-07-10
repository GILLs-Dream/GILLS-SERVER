package com.gills_dream.domain.member.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private LoginType loginType;

    @Column(length = 10)
    @Setter
    private String nickname;

    @Setter
    private String profileImg;

    @Column(nullable = false)
    private String email;

    @Builder.Default
    @Setter
    private Boolean marketingAgreement = false;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RoleType roleType;
}
