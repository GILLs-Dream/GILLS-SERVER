package com.gills_dream.domain.plan.entity;

import com.gills_dream.domain.member.entity.Member;
import com.gills_dream.domain.plan.key.PlanLikeId;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlanLike {

    @EmbeddedId
    private PlanLikeId id;

    @MapsId("memberId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @MapsId("planId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private Plan plan;

    @Column(nullable = false, columnDefinition = "tinyint default 0")
    private Boolean isLiked;

    @Builder
    public PlanLike(Member member, Plan plan, Boolean isLiked) {
        this.member = member;
        this.plan = plan;
        this.isLiked = isLiked != null ? isLiked : false;
        this.id = PlanLikeId.builder()
                .memberId(member.getMemberId())
                .planId(plan.getId())
                .build();
    }
}