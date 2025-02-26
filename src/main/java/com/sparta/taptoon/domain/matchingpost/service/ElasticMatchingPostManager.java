package com.sparta.taptoon.domain.matchingpost.service;

import com.sparta.taptoon.domain.matchingpost.entity.MatchingPost;
import com.sparta.taptoon.domain.matchingpost.entity.document.MatchingPostDocument;
import com.sparta.taptoon.domain.matchingpost.repository.elastic.ElasticAutocompleteRepository;
import com.sparta.taptoon.domain.matchingpost.repository.elastic.ElasticMatchingPostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticMatchingPostManager {

    private final ElasticAutocompleteRepository elasticAutocompleteRepository;

    /**
     * 우선 DB에는 저장이 무조건 되게 하자.
     * Elasticsearch에는 저장이 실패할 수 있다. 그럴 경우는 어떻게 해야 할까?
     * 로그는 우선 남기되, 나중에 스케줄러 같은 걸 이용해서 실패한 데이터 갖고 있다가 한 번에 업데이틀 시도해야 할까? 크흠....
     */

    private final ElasticMatchingPostRepository elasticMatchingPostRepository;

    // DB에 Commit 된 이후에 ES에 저장
    void saveToElasticsearchAfterCommit(MatchingPost newMatchingPost) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    MatchingPostDocument matchingPostDocument = MatchingPostDocument.from(newMatchingPost);
                    elasticMatchingPostRepository.save(matchingPostDocument);
                    log.info("✅ ES 저장 성공: matchingPostId={}", newMatchingPost.getId());
                } catch (Exception e) {
                    // 예외가 발생하면 로깅
                    log.error("❌ ES 저장 실패: matchingPostId={}, error={}", newMatchingPost.getId(), e.getMessage(), e);
                }
            }
        });
    }

    // 없으면 추가
    void upsertToElasticsearchAfterCommit(MatchingPost updatedMatchingPost) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    // 업데이트할 문서 생성
                    MatchingPostDocument updatedDocument = MatchingPostDocument.from(updatedMatchingPost);

                    // ES에 기존 문서가 있는지 확인하고 있으면 업데이트, 없으면 새로 저장
                    elasticMatchingPostRepository.save(updatedDocument);

                    log.info("✅ ES 문서 저장(업데이트 또는 신규 추가) 성공: matchingPostId={}", updatedMatchingPost.getId());
                } catch (Exception e) {
                    log.error("❌ ES 업데이트 중 오류 발생: matchingPostId={}, error={}", updatedMatchingPost.getId(), e.getMessage(), e);
                }
            }
        });
    }

    // `Elasticsearch`에 저장된 포스트 정보 삭제
    void deleteFromElasticsearchAfterCommit(Long matchingPostId) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    // ES에서 해당 문서 삭제
                    elasticMatchingPostRepository.deleteById(matchingPostId);
                    log.info("🗑️ ES 문서 삭제 성공: matchingPostId={}", matchingPostId);
                } catch (Exception e) {
                    log.error("❌ ES 문서 삭제 실패: matchingPostId={}, error={}", matchingPostId, e.getMessage(), e);
                }
            }
        });
    }



}
