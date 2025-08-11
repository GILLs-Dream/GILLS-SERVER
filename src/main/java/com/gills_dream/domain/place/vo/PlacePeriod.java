package com.gills_dream.domain.place.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlacePeriod {
    @Column(columnDefinition = "DATE")
    private LocalDate startedAt;

    @Column(columnDefinition = "DATE")
    private LocalDate finishedAt;

    @Column(columnDefinition = "bigint")
    private Long duration;

    @Builder
    public PlacePeriod(LocalDate startedAt, LocalDate finishedAt, Long duration) {
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.duration = duration;
    }
}
