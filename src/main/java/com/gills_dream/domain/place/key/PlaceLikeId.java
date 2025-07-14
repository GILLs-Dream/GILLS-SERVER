package com.gills_dream.domain.place.key;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@Embeddable
@EqualsAndHashCode
public class PlaceLikeId implements Serializable {

    @Column(name = "place_id")
    private Long placeId;

    @Column(name = "member_id")
    private Long memberId;

    @Builder
    public PlaceLikeId(Long placeId, Long memberId) {
        this.placeId = placeId;
        this.memberId = memberId;
    }
}