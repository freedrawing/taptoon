package com.sparta.taptoon.domain.matchingpost.entity.document;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sparta.taptoon.domain.matchingpost.entity.MatchingPost;
import com.sparta.taptoon.domain.matchingpost.enums.ArtistType;
import com.sparta.taptoon.domain.matchingpost.enums.WorkType;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@ToString
@Slf4j
@Getter
@Document(indexName = "matching-posts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchingPostDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Long, index = false)
    private Long authorId;

    @Field(type = FieldType.Keyword, normalizer = "lowercase")
    private ArtistType artistType;

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "nori", searchAnalyzer = "nori"),
            otherFields = { // 사실 `searchAnalyer`는 NGram이 아니면 따로 설정을 안 하지만, 설정 가독성 차원에서...
                    @InnerField(suffix = "english", type = FieldType.Text, analyzer = "english", searchAnalyzer = "english"),
//                    @InnerField(suffix = "keyword", type = FieldType.Keyword) // 정확한 매칭을 할 때
            }
    )
    private String title;

    @Field(type = FieldType.Keyword, normalizer = "lowercase")
    private WorkType workType;

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "nori", searchAnalyzer = "nori"),
            otherFields = {
                    @InnerField(suffix = "english", type = FieldType.Text, analyzer = "english", searchAnalyzer = "english"),
//                    @InnerField(suffix = "keyword", type = FieldType.Keyword) // 필요 없을 듯
            }
    )
    private String description;

    @Field(type = FieldType.Long) // 정렬하려면 인덱싱 돼야 함
    private Long viewCount;

    @Field(type = FieldType.Keyword, index = false)
    private List<String> fileImageUrlList;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS", timezone = "UTC")
    @Field(type = FieldType.Date, format = DateFormat.epoch_millis)
    private Instant createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS", timezone = "UTC")
    @Field(type = FieldType.Date, format = DateFormat.epoch_millis)
    private Instant updatedAt;


    @Builder
    public MatchingPostDocument(Long id, Long authorId, ArtistType artistType, String title,
                                WorkType workType, String description, Long viewCount,
                                List<String> fileImageUrlList, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.authorId = authorId;
        this.artistType = artistType;
        this.title = title;
        this.workType = workType;
        this.description = description;
        this.viewCount = viewCount;
        this.fileImageUrlList = fileImageUrlList;
        this.createdAt = createdAt.atZone(ZoneId.systemDefault()).toInstant();
        ;
        this.updatedAt = updatedAt.atZone(ZoneId.systemDefault()).toInstant();
    }

    public static MatchingPostDocument from(MatchingPost matchingPost) {
        return MatchingPostDocument.builder()
                .id(matchingPost.getId())
                .authorId(matchingPost.getAuthor().getId())
                .artistType(matchingPost.getArtistType())
                .title(matchingPost.getTitle())
                .workType(matchingPost.getWorkType())
                .description(matchingPost.getDescription())
                .viewCount(matchingPost.getViewCount())
//                .fileImageUrlList(matchingPost.getFileUrlList())
                .fileImageUrlList(List.of("https://static.taptoon.com/1", "https://static.taptoon.com/2", "https://static.taptoon.com/3"))
                .createdAt(matchingPost.getCreatedAt())
                .updatedAt(matchingPost.getUpdatedAt())
                .build();
    }
}
