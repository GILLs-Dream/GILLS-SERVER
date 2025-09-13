package com.gills_dream.domain.plan.repository;

import com.gills_dream.domain.member.entity.Member;
import com.gills_dream.domain.plan.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PlanRepository extends JpaRepository<Plan, Integer> {
    Optional<Plan> findByIdAndMember(Long planId, Member member);
    @Query("select p from Plan p where p.member.id = :memberId and p.summary.summary is not null")
    List<Plan> findAllByMemberAndPlanSummary(Long memberId);
}
