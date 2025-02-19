package com.sparta.taptoon.domain.matchingpost.entity.document;

import com.sparta.taptoon.domain.matchingpost.entity.MatchingPost;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

@Getter
@Document(indexName = "matching-posts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchingPostDocument {

    @Id
    private Long id;

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "nori", searchAnalyzer = "nori"),
            otherFields = { // 사실 `searchAnalyer`는 NGram이 아니면 따로 설정을 안 하지만, 설정 가독성 차원에서...
                    @InnerField(suffix = "english", type = FieldType.Text, analyzer = "english", searchAnalyzer = "english"),
//                    @InnerField(suffix = "keyword", type = FieldType.Keyword)
            }
    )
    private String title;

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "nori", searchAnalyzer = "nori"),
            otherFields = {
                    @InnerField(suffix = "english", type = FieldType.Text, analyzer = "english", searchAnalyzer = "english"),
//                    @InnerField(suffix = "keyword", type = FieldType.Keyword) // 필요 없을 듯
            }
    )
    private String description;

    @Builder
    public MatchingPostDocument(Long id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
    }

    public static MatchingPostDocument from(MatchingPost matchingPost) {
        return MatchingPostDocument.builder()
                .id(matchingPost.getId())
                .title(matchingPost.getTitle())
                .description(matchingPost.getDescription())
                .build();
    }
}
