package com.gills_dream.domain.plan.entity;

import com.gills_dream.domain.place.entity.Place;
import com.gills_dream.domain.place.vo.PlacePeriod;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlanPlace {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "bigint", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "plan_id")
    private Plan plan;

    @ManyToOne
    @JoinColumn(name = "place_id")
    private Place place;

    @Embedded
    private PlacePeriod period;

    @Builder
    public PlanPlace(Plan plan, Place place, PlacePeriod period) {
        this.plan = plan;
        this.place = place;
        this.period = period;
    }

    public void setPeriod(PlacePeriod cur) {
        this.period = cur;
    }
}
