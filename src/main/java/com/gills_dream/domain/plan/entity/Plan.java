package com.gills_dream.domain.plan.entity;

import com.gills_dream.domain.map.dto.MapRouteByDayRes;
import com.gills_dream.domain.member.entity.Member;
import com.gills_dream.domain.plan.enumerate.CategoryType;
import com.gills_dream.domain.plan.enumerate.MoodType;
import com.gills_dream.domain.plan.enumerate.TransportType;
import com.gills_dream.domain.plan.vo.PlanPeriod;
import com.gills_dream.domain.plan.vo.PlanSummary;
import com.gills_dream.domain.team.entity.Team;
import com.gills_dream.global.common.entity.BaseEntity;
import com.gills_dream.global.config.MapRouteByDayResConverter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "`plan`")
@SQLDelete(sql = "UPDATE `plan` SET deleted_at = NOW() WHERE id = ?")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Plan extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "bigint", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    // Todo: Team 구현 후, nullable = false 추가
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    // Todo: Team 구현 후, 삭제
    @Column(columnDefinition = "TEXT")
    private String teamName;

    @Column(columnDefinition = "tinyint")
    private Boolean isPinned;

    @Column(columnDefinition = "bigint")
    private Long party;

    @Column(columnDefinition = "TEXT")
    private String moodSummary;

    @Column(columnDefinition = "TEXT")
    private String region;

    @Embedded
    private PlanPeriod period;

    @Embedded
    private PlanSummary summary;

    @Column
    @Enumerated(EnumType.STRING)
    private TransportType transportType;

    @Convert(converter = MapRouteByDayResConverter.class)
    @Column(columnDefinition = "LONGTEXT")
    @Lob
    private MapRouteByDayRes generatedRoute;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "plan_category", joinColumns = @JoinColumn(name = "plan_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "category_type")
    private List<CategoryType> categories = new ArrayList<>();

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlanPlace> planPlaces = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "plan_mood", joinColumns = @JoinColumn(name = "plan_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "mood_type")
    private List<MoodType> moods = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "plan_video", joinColumns = @JoinColumn(name = "plan_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "video_url")
    private List<String> videoIds = new ArrayList<>();

    @Builder
    public Plan(Member member, Team team, PlanSummary summary, PlanPeriod period, String moodSummary, String teamName, String region,
                Boolean isPinned, Long party, TransportType transportType, List<CategoryType> categories, List<MoodType> moods, List<String> videoIds, MapRouteByDayRes generatedRoute) {
        this.member = member;
        this.team = team;
        this.summary = summary;
        this.moodSummary = moodSummary;
        this.teamName = teamName;
        this.period = period;
        this.region = region;
        this.isPinned = isPinned;
        this.party = party;
        this.transportType = transportType;
        this.categories = categories != null ? new ArrayList<>(categories) : new ArrayList<>();
        this.moods = moods != null ? new ArrayList<>(moods) : new ArrayList<>();
        this.videoIds = videoIds != null ? new ArrayList<>(videoIds) : new ArrayList<>();
        this.generatedRoute = generatedRoute;
    }

    public void updatePeriod(PlanPeriod period) {
        this.period = period;
    }

    public void updateTransportType(TransportType transportType) {
        this.transportType = transportType;
    }

    public void updateCompanion(String companion, Long party) {
        this.teamName = companion;
        this.party = party;
    }

    public void updateRegion(String region) {
        this.region = region;
    }

    public void updateGeneratedRoute(MapRouteByDayRes generatedRoute) {
        this.generatedRoute = generatedRoute;
    }

    public void updateMember(Member member) {
        this.member = member;
    }

    public void updateSummary(PlanSummary summary) {
        this.summary = summary;
    }

    public void updateMoods(List<MoodType> moodTypeList) {
        this.moods.addAll(moodTypeList);
    }

    public void updateMoodSummary(String summary) {
        this.moodSummary = summary;
    }
}