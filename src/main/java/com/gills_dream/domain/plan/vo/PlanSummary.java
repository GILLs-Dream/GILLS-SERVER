package com.gills_dream.domain.plan.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlanSummary {
    @Column(columnDefinition = "varchar(100)")
    private String title;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String mainImg;

    @Builder
    public PlanSummary(String title, String summary, String mainImg) {
        this.title = title;
        this.summary = summary;
        this.mainImg = mainImg;
    }

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateSummary(String summary) {
        this.summary = summary;
    }

    public void updateMainImg(String mainImg) {
        this.mainImg = mainImg;
    }
}