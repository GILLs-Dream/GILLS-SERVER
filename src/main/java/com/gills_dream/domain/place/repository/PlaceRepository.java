package com.gills_dream.domain.place.repository;

import com.gills_dream.domain.place.entity.Place;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlaceRepository extends JpaRepository<Place, Long> {
    @Query("SELECT p FROM Place p " +
            "WHERE p.name = :name " +
            "AND ROUND(cast(p.locX as double), 2) = ROUND(:locX, 2) " +
            "AND ROUND(cast(p.locY as double), 2) = ROUND(:locY, 2)")
    Optional<Place> findByNameAndLocXAndLocYRounded(@Param("name") String name,
                                                    @Param("locX") double locX,
                                                    @Param("locY") double locY);

    @Query("SELECT p FROM Place p LEFT JOIN p.planPlaces pp WHERE pp.plan.id = :planId")
    List<Place> findAllByPlanId(@Param("planId") Long planId);
}
