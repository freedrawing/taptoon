package com.sparta.taptoon.domain.matchingpost.entity;

import com.sparta.taptoon.global.common.BaseEntity;
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
    @ManyToOne
    private MatchingPost matchingPost;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Builder
    public MatchingPostImage(MatchingPost matchingPost, String imageUrl) {
        this.matchingPost = matchingPost;
        this.imageUrl = imageUrl;
    }
}
