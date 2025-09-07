package com.gills_dream.domain.plan.repository;

import com.gills_dream.domain.member.entity.Member;
import com.gills_dream.domain.plan.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PlanRepository extends JpaRepository<Plan, Integer> {
    Optional<Plan> findByIdAndMember(Long planId, Member member);
    Optional<Plan> findById(Long planId);
}
