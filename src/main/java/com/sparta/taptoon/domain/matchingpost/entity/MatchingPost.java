package com.sparta.taptoon.domain.matchingpost.entity;

import com.sparta.taptoon.domain.matchingpost.enums.ArtistType;
import com.sparta.taptoon.domain.matchingpost.enums.WorkType;
import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "matching_post")
public class MatchingPost extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Member writer;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "artist_type")
    private ArtistType artistType;

    @Column(name = "title", nullable = false)
    private String title;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "work_type", nullable = false)
    private WorkType workType;

    @Column(name = "file_url")
    private String fileUrl;

    @Column(name = "description", nullable = false, length = 3_000)
    private String description;

    @Column(name = "view_count", nullable = false)
    private Long viewCount;

    @Column(name = "is_delete", nullable = false)
    private Boolean isDeleted;


    @Builder
    public MatchingPost(Member writer, ArtistType artistType, String title,
                        WorkType workType, String fileUrl, String description, Long viewCount, Boolean isDeleted) {

        this.writer = writer;
        this.artistType = artistType;
        this.title = title;
        this.workType = workType;
        this.fileUrl = fileUrl;
        this.description = description;
        this.viewCount = viewCount;
        this.isDeleted = true;
    }
}
