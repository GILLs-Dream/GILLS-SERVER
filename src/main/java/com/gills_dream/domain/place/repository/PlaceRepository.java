package com.gills_dream.domain.place.repository;

import com.gills_dream.domain.place.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PlaceRepository extends JpaRepository<Place, Long> {
    @Query("SELECT p FROM Place p " +
            "WHERE p.name = :name " +
            "AND ROUND(p.locX, 2) = ROUND(:locX, 2) " +
            "AND ROUND(p.locY, 2) = ROUND(:locY, 2)")
    Optional<Place> findByNameAndLocXAndLocYRounded(@Param("name") String name,
                                                    @Param("locX") double locX,
                                                    @Param("locY") double locY);
}
