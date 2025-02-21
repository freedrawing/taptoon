package com.sparta.taptoon.domain.matchingpost.entity.document;

import com.sparta.taptoon.domain.matchingpost.entity.MatchingPost;
import com.sparta.taptoon.domain.matchingpost.enums.ArtistType;
import com.sparta.taptoon.domain.matchingpost.enums.WorkType;
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
//@Setting(settingPath = "/elastic/matchingpost-setting.json") // Elasticsearch 버전에 오류가 많아서 이 방법은 안 될 듯하다...
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchingPostDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Long, index = false)
    private Long authorId;

    @Field(type = FieldType.Keyword, normalizer = "lowercase")
    private ArtistType artistType;

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "nori"),
            otherFields = {
                    @InnerField(suffix = "ngram", type = FieldType.Text, analyzer = "ngram_analyzer"), // 이건 ES에 미리 안 만들어지면 서버 동작 안 함
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

    @Field(type = FieldType.Long) // 정렬하려면 인덱싱 돼야 함
    private Long viewCount;

    @Field(type = FieldType.Keyword, index = false)
    private List<String> fileImageUrlList;

    // 나중에 정렬할 때 속도가 너무 느리면 `epoch_millis`로 바꾸자
    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS||epoch_millis")
    private LocalDateTime createdAt;

    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS||epoch_millis")
    private LocalDateTime updatedAt;


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
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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
