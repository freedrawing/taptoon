package com.sparta.taptoon.domain.matchingpost.entity;

import com.sparta.taptoon.domain.matchingpost.dto.request.RegisterMatchingPostRequest;
import com.sparta.taptoon.domain.matchingpost.dto.request.UpdateMatchingPostRequest;
import com.sparta.taptoon.domain.matchingpost.dto.response.MatchingPostImageResponse;
import com.sparta.taptoon.domain.matchingpost.enums.ArtistType;
import com.sparta.taptoon.domain.matchingpost.enums.WorkType;
import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.global.common.BaseEntity;
import com.sparta.taptoon.global.common.enums.Status;
import com.sparta.taptoon.global.error.exception.NotFoundException;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.sparta.taptoon.global.error.enums.ErrorCode.*;

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
    private Member author;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "artist_type")
    private ArtistType artistType;

    @Column(name = "title", nullable = false)
    private String title;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "work_type", nullable = false)
    private WorkType workType;

    @Column(name = "description", nullable = false, length = 3_000)
    private String description;

    @Column(name = "view_count", nullable = false)
    private Long viewCount;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @OneToMany(mappedBy = "matchingPost", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MatchingPostImage> matchingPostImages = new ArrayList<>();


    @Builder
    public MatchingPost(Member author, ArtistType artistType, String title, WorkType workType, String description) {

        this.author = author;
        this.artistType = artistType;
        this.title = title;
        this.workType = workType;
        this.description = description;
        this.viewCount = 0L;
        this.status = Status.PENDING; // 처음에는 등록 대기 상태
    }

    public boolean isMyMatchingPost(Long memberId) {
        return author.getId() == memberId;
    }

    public void removeMe() {
        status = Status.DELETED;
    }

    public void registerMe(RegisterMatchingPostRequest request) {
        this.title = request.title();
        this.artistType = ArtistType.of(request.artistType());
        this.workType = WorkType.of(request.workType());
        this.description = request.description();
        status = Status.REGISTERED;
        updateCreatedAtToNow();
    }

    public void editMe(UpdateMatchingPostRequest request) {
        this.title = request.title();
        this.artistType = ArtistType.of(request.artistType());
        this.workType = WorkType.of(request.workType());
        this.description = request.description();
    }

    public void increaseViewCount() {
        viewCount++;
    }

    public void validateIsDeleted() {
        if (Status.isDeleted(status)) {
            throw new NotFoundException(MATCHING_POST_NOT_FOUND);
        }
    }
}
