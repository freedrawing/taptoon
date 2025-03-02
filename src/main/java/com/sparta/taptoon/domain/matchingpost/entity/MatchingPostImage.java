package com.sparta.taptoon.domain.matchingpost.entity;

import com.sparta.taptoon.global.common.BaseEntity;
import com.sparta.taptoon.global.common.enums.Status;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "matching_post_image")
@NoArgsConstructor
public class MatchingPostImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "matching_post_id", nullable = false, updatable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private MatchingPost matchingPost;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "thumbnail_image_url", nullable = false, length = 1000)
    private String thumbnailImageUrl;

    @Column(name = "original_image_url", nullable = false, length = 1000)
    private String originalImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Builder
    public MatchingPostImage(MatchingPost matchingPost, String fileName, String thumbnailImageUrl, String originalImageUrl) {
        this.matchingPost = matchingPost;
        this.fileName = fileName;
        this.thumbnailImageUrl = thumbnailImageUrl;
        this.originalImageUrl = originalImageUrl;
        this.status = Status.PENDING; // 처음은 이미지 저장 대기 상태
    }

    public void registerMe() {
        status = Status.REGISTERED;
        updateCreatedAtToNow();
    }
}
