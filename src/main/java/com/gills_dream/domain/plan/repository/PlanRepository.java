package com.gills_dream.domain.plan.repository;

import com.gills_dream.domain.plan.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanRepository extends JpaRepository<Plan, Integer> {
}
