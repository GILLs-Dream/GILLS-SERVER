package com.gills_dream.domain.plan.repository;

import com.gills_dream.domain.plan.entity.PlanPlace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlanPlaceRepository extends JpaRepository<PlanPlace, Integer> {
    boolean existsByPlanIdAndPlaceId(Long planId, Long placeId);
    Optional<PlanPlace> findByPlanIdAndPlaceId(Long id, Long id1);
}
