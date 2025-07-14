package com.gills_dream.domain.team.key;

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
public class MemberTeamId implements Serializable {

    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "team_id")
    private Long teamId;

    @Builder
    public MemberTeamId(Long memberId, Long teamId) {
        this.memberId = memberId;
        this.teamId = teamId;
    }
}