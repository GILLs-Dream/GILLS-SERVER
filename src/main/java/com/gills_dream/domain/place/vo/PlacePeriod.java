package com.gills_dream.domain.place.vo;

import com.gills_dream.domain.map.dto.MapRouteReq;
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
    private Integer duration;

    @Column(columnDefinition = "bigint")
    private Integer dayOrder;

    @Builder
    public PlacePeriod(LocalDate startedAt, LocalDate finishedAt, Integer duration, Integer dayOrder) {
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.duration = duration;
        this.dayOrder = dayOrder;
    }

    public void setStartedAt(LocalDate startedAt) {
        this.startedAt = startedAt;
    }

    public void setFinishedAt(LocalDate finishedAt) {
        this.finishedAt = finishedAt;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public void setDayOrder(Integer dayOrder) {
        this.dayOrder = dayOrder;
        this.dayOrder = dayOrder;
    }

    public void updateDate(MapRouteReq.Period period) {
        this.startedAt = LocalDate.parse(period.startedAt());
        this.finishedAt = LocalDate.parse(period.finishedAt());
        this.duration = period.duration();
        this.dayOrder = period.dayOrder();
    }
}
