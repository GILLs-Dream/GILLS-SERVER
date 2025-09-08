package com.gills_dream.domain.plan.dto;

import com.gills_dream.domain.plan.entity.Plan;
import lombok.Builder;

import java.util.List;

@Builder
public record PlanListResponseDto (
        List<PlanSummaryRes> planList
) {
    @Builder
    public record PlanSummaryRes (
            Long planId,
            String title,
            String startedAt,
            String finishedAt,
            String thumbnailUrl
    ) {
        public static PlanSummaryRes fromEntity(Plan plan) {
            return PlanSummaryRes.builder()
                    .planId(plan.getId())
                    .title(plan.getSummary() != null && plan.getSummary().getTitle() != null
                            ? plan.getSummary().getTitle()
                            : "")
                    .startedAt(plan.getPeriod() != null && plan.getPeriod().getStartedAt() != null
                            ? plan.getPeriod().getStartedAt().toString()
                            : "")
                    .finishedAt(plan.getPeriod() != null && plan.getPeriod().getFinishedAt() != null
                            ? plan.getPeriod().getFinishedAt().toString()
                            : "")
                    .thumbnailUrl(plan.getSummary() != null && plan.getSummary().getThumbnailUrl() != null
                            ? plan.getSummary().getThumbnailUrl()
                            : "")
                    .build();
        }
    }
    public static PlanListResponseDto fromEntityList(List<Plan> planList) {
        return PlanListResponseDto.builder()
                .planList(planList.stream().map(PlanSummaryRes::fromEntity).toList())
                .build();
    }
}
