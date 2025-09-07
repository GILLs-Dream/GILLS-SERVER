package com.gills_dream.domain.plan.entity;

import com.gills_dream.domain.member.entity.Member;
import com.gills_dream.domain.place.entity.Place;
import com.gills_dream.domain.place.enumerate.Region;
import com.gills_dream.domain.plan.enumerate.CategoryType;
import com.gills_dream.domain.plan.enumerate.MoodType;
import com.gills_dream.domain.plan.enumerate.TransportType;
import com.gills_dream.domain.plan.vo.PlanPeriod;
import com.gills_dream.domain.plan.vo.PlanSummary;
import com.gills_dream.domain.team.entity.Team;
import com.gills_dream.global.common.entity.BaseEntity;
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
@SQLDelete(sql = "UPDATE \"plan\" SET deleted_at = NOW() WHERE id = ?")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Plan extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "bigint", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
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
    private String mooodSummary;

    @Column
    private String region;

    @Embedded
    private PlanPeriod period;

    @Embedded
    private PlanSummary summary;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "plan_transport", joinColumns = @JoinColumn(name = "plan_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "transport_type")
    private List<TransportType> transports = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "plan_category", joinColumns = @JoinColumn(name = "plan_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "category_type")
    private List<CategoryType> categories = new ArrayList<>();

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL)
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

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "plan_content_id", joinColumns = @JoinColumn(name = "plan_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "content_id")
    private List<String> contentIds = new ArrayList<>();

    @Builder
    public Plan(Member member, Team team, PlanSummary summary, PlanPeriod period, String moodSummary, String teamName, String region,
                Boolean isPinned, Long party, List<TransportType> transports, List<CategoryType> categories, List<MoodType> moods, List<String> videoIds, List<String> contentIds) {
        this.member = member;
        this.team = team;
        this.summary = summary;
        this.mooodSummary = moodSummary;
        this.teamName = teamName;
        this.period = period;
        this.region = region;
        this.isPinned = isPinned;
        this.party = party;
        this.transports = transports != null ? new ArrayList<>(transports) : new ArrayList<>();
        this.categories = categories != null ? new ArrayList<>(categories) : new ArrayList<>();
        this.moods = moods != null ? new ArrayList<>(moods) : new ArrayList<>();
        this.videoIds = videoIds != null ? new ArrayList<>(videoIds) : new ArrayList<>();
        this.contentIds = contentIds != null ? new ArrayList<>(contentIds) : new ArrayList<>();
    }

    public void updatePeriod(PlanPeriod period) {
        this.period = period;
    }

    public void updateCompanion(String companion, Long party) {
        this.teamName = companion;
        this.party = party;
    }

    public void updateRegion(String region) {
        this.region = region;
    }
}