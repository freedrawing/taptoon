package com.sparta.taptoon.domain.matchingpost.entity;

import com.sparta.taptoon.domain.matchingpost.dto.request.RegisterMatchingPostRequest;
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

    @Column(name = "file_url")
    private String fileUrl; // 이거 필요 없을 듯

    @Column(name = "description", nullable = false, length = 3_000)
    private String description;

    @Column(name = "view_count", nullable = false)
    private Long viewCount;

    @Column(name = "is_delete", nullable = false)
    private Boolean isDeleted;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @OneToMany(mappedBy = "matchingPost", cascade = CascadeType.ALL, orphanRemoval = true) // Elasticsearch 용도
    private List<MatchingPostImage>matchingPostImages = new ArrayList<>();


    @Builder
    public MatchingPost(Member author, ArtistType artistType, String title,
                        WorkType workType, String fileUrl, String description) {

        this.author = author;
        this.artistType = artistType;
        this.title = title;
        this.workType = workType;
        this.fileUrl = fileUrl;
        this.description = description;
        this.viewCount = 0L;
        this.isDeleted = false;
        this.status = Status.PENDING; // 처음에는 등록 대기 상태
    }

    // id 비교는 따로 쿼리가 안 날라감
    public boolean isMyMatchingPost(Long memberId) {
        return author.getId() == memberId;
    }

    public void removeMe() {
        isDeleted = true;
    }

    // 수정처럼 동작하지만 사실상 등록임. 빈 엔티티가 처음에 만들어지므로
    public void registerMe(RegisterMatchingPostRequest request) {
        this.title = request.title();
        this.artistType = ArtistType.of(request.artistType());
        this.workType = WorkType.of(request.workType());
        this.description = request.description();
        status = Status.REGISTERED;
    }

    // TODO: MatchingPost 수정용 메서드도 만들어야 함

    public void increaseViewCount() {
        viewCount++;
    }

    public void validateIsDeleted() {
        if (isDeleted) {
            throw new NotFoundException(MATCHING_POST_NOT_FOUND);
        }
    }

    public List<String> getFileUrlList() {
        return matchingPostImages.stream()
                .filter(matchingPostImage -> Status.isRegistered(matchingPostImage.getStatus()))
                .map(matchingPostImage -> matchingPostImage.getImageUrl())
                .toList();
    }
}
