package com.sparta.taptoon.domain.matchingpost.entity.document;

import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.elasticsearch.annotations.*;

import java.time.LocalDateTime;

@Getter
@Document(indexName = "autocomplete")
public class AutocompleteDocument {

    @Id
    @MultiField(
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword),
            }
    )
    private String word;

    @Field(type = FieldType.Long, index = false)
    private Long searchCount;

    @CreatedDate
    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS||epoch_millis")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Field(type = FieldType.Date, format = {}, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS||epoch_millis")
    private LocalDateTime updatedAt;

    public AutocompleteDocument(String word) {
        this.word = word;
        searchCount = 0L;
        createdAt = LocalDateTime.now(); // @CreatedDate가 동작을 안 한다.... Elasticsearch는 생각보다 버그가 많은 듯하다
    }

    public void increaseSearchCount() {
        searchCount++;
    }
}
