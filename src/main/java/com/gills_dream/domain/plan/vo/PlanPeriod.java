package com.gills_dream.domain.plan.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlanPeriod {
    @Column(columnDefinition = "DATE")
    private LocalDate startedAt;

    @Column(columnDefinition = "DATE")
    private LocalDate finishedAt;

    @Column(columnDefinition = "bigint")
    private Long duration;

    @Builder
    public PlanPeriod(LocalDate startedAt, LocalDate finishedAt, Long duration) {
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.duration = duration;
    }
}
