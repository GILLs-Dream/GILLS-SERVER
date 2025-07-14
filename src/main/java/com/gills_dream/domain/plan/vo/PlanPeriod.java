package com.gills_dream.domain.plan.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.LocalDateTime;

@Embeddable
public class PlanPeriod {
    @Column(columnDefinition = "Datetime")
    private LocalDateTime startedAt;

    @Column(columnDefinition = "Datetime")
    private LocalDateTime finishedAt;

    @Column(columnDefinition = "bigint")
    private Long duration;
}
