package com.sparta.taptoon.domain.matchingpost.entity.document;

import com.sparta.taptoon.domain.matchingpost.dto.response.MatchingPostImageResponse;
import com.sparta.taptoon.domain.matchingpost.entity.MatchingPost;
import com.sparta.taptoon.domain.matchingpost.enums.ArtistType;
import com.sparta.taptoon.domain.matchingpost.enums.WorkType;
import com.sparta.taptoon.global.common.enums.Status;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@ToString
@Slf4j
@Getter
@Document(indexName = "matching_post")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchingPostDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Long, index = false)
    private Long authorId;

    @Field(type = FieldType.Text)
    private String authorName;

    @Field(type = FieldType.Keyword, normalizer = "lowercase")
    private ArtistType artistType;

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "nori"),
            otherFields = {
                    @InnerField(suffix = "ngram", type = FieldType.Text, analyzer = "ngram_analyzer"),
                    @InnerField(suffix = "english", type = FieldType.Text, analyzer = "english"),
            }
    )
    private String title;

    @Field(type = FieldType.Keyword, normalizer = "lowercase")
    private WorkType workType;

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "nori"),
            otherFields = {
                    @InnerField(suffix = "ngram", type = FieldType.Text, analyzer = "ngram_analyzer"),
                    @InnerField(suffix = "english", type = FieldType.Text, analyzer = "english"),
            }
    )
    private String description;

    @Field(type = FieldType.Long)
    private Long viewCount;

    @Field(type = FieldType.Nested, index = false)
    private List<MatchingPostImageResponse> imageList;

    // 나중에 정렬할 때 속도가 너무 느리면 `epoch_millis`로 바꾸자
    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS||epoch_millis")
    private LocalDateTime createdAt;

    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS||epoch_millis")
    private LocalDateTime updatedAt;


    @Builder
    public MatchingPostDocument(Long id, Long authorId, String authorName, ArtistType artistType, String title,
                                WorkType workType, String description, Long viewCount, List<MatchingPostImageResponse> imageList,
                                LocalDateTime createdAt, LocalDateTime updatedAt) {

        this.id = id;
        this.authorId = authorId;
        this.authorName= authorName;
        this.artistType = artistType;
        this.title = title;
        this.workType = workType;
        this.description = description;
        this.viewCount = viewCount;
        this.imageList = imageList;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static MatchingPostDocument from(MatchingPost matchingPost) {
        return MatchingPostDocument.builder()
                .id(matchingPost.getId())
                .authorId(matchingPost.getAuthor().getId())
                .authorName(matchingPost.getAuthor().getName())
                .artistType(matchingPost.getArtistType())
                .title(matchingPost.getTitle())
                .workType(matchingPost.getWorkType())
                .description(matchingPost.getDescription())
                .viewCount(matchingPost.getViewCount())
                .imageList(
                        matchingPost.getMatchingPostImages().stream()
                                .filter(matchingPostImage -> Status.isRegistered(matchingPostImage.getStatus()))
                                .map(MatchingPostImageResponse::from)
                                .toList()
                )
                .createdAt(matchingPost.getCreatedAt())
                .updatedAt(matchingPost.getUpdatedAt())
                .build();
    }
}
