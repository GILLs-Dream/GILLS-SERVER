package com.gills_dream.domain.team.entity;

import com.gills_dream.domain.member.entity.Member;
import com.gills_dream.domain.team.enumerate.MemberTeamRole;
import com.gills_dream.domain.team.key.MemberTeamId;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class MemberTeam {

    @EmbeddedId
    private MemberTeamId id;

    @MapsId("memberId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @MapsId("teamId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @Enumerated(EnumType.STRING)
    private MemberTeamRole role;

    @Builder
    public MemberTeam(Member member, Team team, MemberTeamRole role) {
        this.member = member;
        this.team = team;
        this.role = role;
        this.id = MemberTeamId.builder()
                .memberId(member.getId())
                .teamId(team.getId())
                .build();
    }
}