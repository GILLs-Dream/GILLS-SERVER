package com.gills_dream.domain.member.entity;

import com.gills_dream.domain.plan.entity.Plan;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 10)
    private String nickname;

    private String profileImg;

    @Column(nullable = false)
    private String email;

    private String sub;

    private Boolean marketingAgreement = false;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RoleType roleType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private LoginType loginType;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Plan> planList = new ArrayList<>();

    @Builder
    public Member(String email, LoginType loginType, RoleType roleType, String sub) {
        this.email = email;
        this.loginType = loginType;
        this.roleType = roleType;
        this.sub = sub;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateProfileImg(String profileImg) {
        this.profileImg = profileImg;
    }

    public void updateMarketingAgreement(Boolean marketingAgreement) {
        this.marketingAgreement = marketingAgreement;
    }

    public void updateEmail(String email) {
        this.email = email;
    }
}
