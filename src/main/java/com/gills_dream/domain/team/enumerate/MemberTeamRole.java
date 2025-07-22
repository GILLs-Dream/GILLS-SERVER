package com.gills_dream.domain.team.enumerate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberTeamRole {
    MEMBER("팀원"),
    OWNER("팀장");

    private final String description;
}
