package com.gills_dream.domain.plan.key;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@Embeddable
@EqualsAndHashCode
public class PlanLikeId implements Serializable {

    @Column(name = "plan_id")
    private Long planId;

    @Column(name = "member_id")
    private Long memberId;

    @Builder
    public PlanLikeId(Long planId, Long memberId) {
        this.planId = planId;
        this.memberId = memberId;
    }
}