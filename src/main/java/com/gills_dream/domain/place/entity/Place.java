package com.gills_dream.domain.place.entity;

import com.gills_dream.domain.place.enumerate.PlaceType;
import com.gills_dream.domain.place.enumerate.Platform;
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

    @Column(columnDefinition = "TEXT")
    private String contentId;

    @Column
    @Enumerated(EnumType.STRING)
    private Platform platform;

    @Column(columnDefinition = "varchar(100)", nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    @Lob
    private String detailUrl;

    @Column(columnDefinition = "TEXT")
    private String lat;

    @Column(columnDefinition = "TEXT")
    private String lng;

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
    public Place(String name, String contentId, Platform platform, String detailUrl, String lat, String lng, PlaceType placeType, List<String> images) {
        this.name = name;
        this.contentId = contentId;
        this.platform = platform;
        this.detailUrl = detailUrl;
        this.lat = lat;
        this.lng = lng;
        this.placeType = placeType;
        this.images = images != null ? images : new ArrayList<>();
    }

    public void addImage(String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            this.images.add(imageUrl);
        }
    }
}