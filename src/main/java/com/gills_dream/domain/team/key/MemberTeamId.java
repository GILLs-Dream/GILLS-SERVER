package com.gills_dream.domain.team.key;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MemberTeamId that)) return false;
        return Objects.equals(memberId, that.memberId) &&
                Objects.equals(teamId, that.teamId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memberId, teamId);
    }
}