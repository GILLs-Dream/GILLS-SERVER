package com.gills_dream.domain.place.entity;

import com.gills_dream.domain.place.enumerate.PlaceType;
import com.gills_dream.domain.place.enumerate.Region;
import com.gills_dream.domain.place.vo.PlacePeriod;
import com.gills_dream.domain.plan.entity.PlanPlace;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Place {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "bigint", nullable = false)
    private Long id;

    @Column(columnDefinition = "varchar(100)", nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private double locX;

    @Column(columnDefinition = "TEXT")
    private double locY;

    @Column
    @Enumerated(EnumType.STRING)
    private PlaceType placeType;

    @OneToMany(mappedBy = "place")
    private List<PlanPlace> planPlaces = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "place_image", joinColumns = @JoinColumn(name = "place_id"))
    @Column(name = "image_url")
    private List<String> images = new ArrayList<>();

    @Builder
    public Place(String name, String description, double locX, double locY, PlaceType placeType, List<String> images) {
        this.name = name;
        this.description = description;
        this.locX = locX;
        this.locY = locY;
        this.placeType = placeType;
        this.images = images != null ? images : new ArrayList<>();
    }

    public void addImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            this.images.add(imageUrl);
        }
    }
}