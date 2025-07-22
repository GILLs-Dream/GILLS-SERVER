package com.gills_dream.domain.plan.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Builder;

import java.time.LocalDateTime;

@Embeddable
public class PlanPeriod {
    @Column(columnDefinition = "DATETIME")
    private LocalDateTime startedAt;

    @Column(columnDefinition = "DATETIME")
    private LocalDateTime finishedAt;

    @Column(columnDefinition = "bigint")
    private Long duration;

    @Builder
    public PlanPeriod(LocalDateTime startedAt, LocalDateTime finishedAt, Long duration) {
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.duration = duration;
    }
}
