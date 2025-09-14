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
            String mainImg
    ) {
        public static PlanSummaryRes fromEntity(Plan plan) {
            return PlanSummaryRes.builder()
                    .planId(plan.getId())
                    .title(plan.getMoodSummary() != null ? plan.getMoodSummary() : "")
                    .startedAt(plan.getPeriod() != null && plan.getPeriod().getStartedAt() != null
                            ? plan.getPeriod().getStartedAt().toString()
                            : "")
                    .finishedAt(plan.getPeriod() != null && plan.getPeriod().getFinishedAt() != null
                            ? plan.getPeriod().getFinishedAt().toString()
                            : "")
                    .mainImg(plan.getSummary() != null && plan.getSummary().getMainImg() != null
                            ? plan.getSummary().getMainImg()
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
