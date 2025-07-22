package com.gills_dream.domain.team.entity;

import com.gills_dream.global.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;

@Getter
@Entity
@SQLDelete(sql = "UPDATE \"team\" SET deleted_at = NOW() WHERE id = ?")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Team extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(columnDefinition = "bigint", nullable = false)
    private Long id;

    @Column(columnDefinition = "varchar(100)")
    private String name;

    @Column(columnDefinition = "varchar(100)")
    private String description;

    @Column(columnDefinition = "text")
    private String profileImageUrl;

    @Column(columnDefinition = "varchar(100)")
    private String code;

    @Builder
    public Team(String name, String description, String profileImageUrl, String code) {
        this.name = name;
        this.description = description;
        this.profileImageUrl = profileImageUrl;
        this.code = code;
    }
}
