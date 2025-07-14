package com.gills_dream.domain.plan.key;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Getter
@NoArgsConstructor
@Embeddable
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlanLikeId that)) return false;
        return Objects.equals(planId, that.planId) &&
                Objects.equals(memberId, that.memberId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(planId, memberId);
    }
}