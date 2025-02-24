package com.sparta.taptoon.domain.matchingpost.service;

import com.sparta.taptoon.domain.matchingpost.entity.document.AutocompleteDocument;
import com.sparta.taptoon.domain.matchingpost.repository.elastic.ElasticAutocompleteRepository;
import com.sparta.taptoon.global.common.annotation.DistributedLock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ElasticAutocompleteService {

    private final ElasticAutocompleteRepository elasticAutocompleteRepository;

    // 검색어 저장
    @DistributedLock(key = "#keyword")
    public void logSearchQuery(String keyword) {
        if (StringUtils.hasText(keyword)) {
            AutocompleteDocument document = elasticAutocompleteRepository.findById(keyword)
                    .orElse(new AutocompleteDocument(keyword));
            document.increaseSearchCount(); // 검색수 증가시키고

            // JPA가 아니기 때문에 다시 저장해줘야 함
            elasticAutocompleteRepository.save(document);
        }
    }

    public List<String> findAutocompleteSuggestion(String keyword) {
        if (StringUtils.hasText(keyword) == false) {
            return Collections.emptyList();
        }

        return elasticAutocompleteRepository.searchAutocomplete(keyword);
    }
}
