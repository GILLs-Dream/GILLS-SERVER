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
            "AND ROUND(cast(p.lng as double), 2) = ROUND(:lng, 2) " +
            "AND ROUND(cast(p.lat as double), 2) = ROUND(:lat, 2)")
    Optional<Place> findByNameAndLatAndLngRounded(@Param("name") String name,
                                                    @Param("lat") double lat,
                                                    @Param("lng") double lng);

    @Query("""
    select distinct p
    from Place p
    join fetch p.planPlaces pp
    join fetch pp.plan pl
    where pl.id = :planId
""")
    List<Place> findAllByPlanId(@Param("planId") Long planId);

    @Query("""
    select distinct p
    from Place p
    left join fetch p.images imgs
    where p.id in :ids
""")
    List<Place> findAllWithImagesByIdIn(@Param("ids") List<Long> ids);
}
