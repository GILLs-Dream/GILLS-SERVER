package com.gills_dream.domain.plan.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlanSummary {
    @Column(columnDefinition = "varchar(100)")
    private String title;

    @Column(columnDefinition = "TEXT")
    private String detail;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String thumbnailUrl;

    // Todo: 정책 정해지면, enum으로 변경
    @Column(columnDefinition = "varchar(50)")
    private String mood;

    @Builder
    public PlanSummary(String title, String detail, String summary, String thumbnailUrl, String mood) {
        this.title = title;
        this.detail = detail;
        this.summary = summary;
        this.thumbnailUrl = thumbnailUrl;
        this.mood = mood;
    }
}