package com.gills_dream.domain.place.repository;

import com.gills_dream.domain.place.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceRepostiory extends JpaRepository<Place, Long> {
}
