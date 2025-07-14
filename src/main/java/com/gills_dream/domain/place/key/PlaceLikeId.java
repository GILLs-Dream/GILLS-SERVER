package com.gills_dream.domain.place.key;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

@Getter
@NoArgsConstructor
@Embeddable
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlaceLikeId that)) return false;
        return Objects.equals(placeId, that.placeId) &&
                Objects.equals(memberId, that.memberId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(placeId, memberId);
    }
}