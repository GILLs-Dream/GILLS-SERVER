package com.gills_dream.domain.place.entity;

import com.gills_dream.domain.member.entity.Member;
import com.gills_dream.domain.place.key.PlaceLikeId;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaceLike {
    @EmbeddedId
    private PlaceLikeId id;

    @MapsId("memberId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @MapsId("placeId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    private Place place;

    @Column(nullable = false, columnDefinition = "tinyint default 0")
    private Boolean isLiked;

    @Builder
    public PlaceLike(Member member, Place place, Boolean isLiked) {
        this.member = member;
        this.place = place;
        this.isLiked = isLiked != null ? isLiked : false;
        this.id = PlaceLikeId.builder()
                .memberId(member.getMemberId())
                .placeId(place.getId())
                .build();
    }
}