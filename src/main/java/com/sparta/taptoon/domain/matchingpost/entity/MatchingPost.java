package com.sparta.taptoon.domain.matchingpost.entity;

import com.sparta.taptoon.domain.matchingpost.dto.request.UpdateMatchingPostRequest;
import com.sparta.taptoon.domain.matchingpost.enums.ArtistType;
import com.sparta.taptoon.domain.matchingpost.enums.WorkType;
import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.global.common.BaseEntity;
import com.sparta.taptoon.global.error.enums.ErrorCode;
import com.sparta.taptoon.global.error.exception.NotFoundException;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
                        WorkType workType, String fileUrl, String description) {

        this.writer = writer;
        this.artistType = artistType;
        this.title = title;
        this.workType = workType;
        this.fileUrl = fileUrl;
        this.description = description;
        this.viewCount = 0L;
        this.isDeleted = false;
    }

    // id 비교는 따로 쿼리가 안 날라감
    public boolean isMyMatchingPost(Long userId) {
        return writer.getId() == userId;
    }

    public void removeMe() {
        isDeleted = true;
    }

    public void modifyMe(UpdateMatchingPostRequest request) {
        this.title = request.title();
        this.workType = WorkType.of(request.workType());
        this.description = request.description();
    }

    public void increaseViewCount() {
        viewCount++;
    }

    public void validateIsDeleted() {
        if (isDeleted) {
            throw new NotFoundException(MATCHING_POST_NOT_FOUND);
        }
    }
}
