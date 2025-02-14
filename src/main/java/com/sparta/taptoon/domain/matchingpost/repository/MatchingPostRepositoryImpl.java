package com.sparta.taptoon.domain.matchingpost.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.taptoon.domain.matchingpost.dto.response.MatchingPostResponse;
import com.sparta.taptoon.domain.matchingpost.enums.ArtistType;
import com.sparta.taptoon.domain.matchingpost.enums.WorkType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

import static com.sparta.taptoon.domain.matchingpost.entity.QMatchingPost.matchingPost;

@RequiredArgsConstructor
public class MatchingPostRepositoryImpl implements MatchingPostRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    /*
     * TODO)
     * 이미지 혹은 텍스트 파일 여러장 반환될 수 있게 나중에 고쳐야 함
     * Projections.constructor 대신에 @QueryProjection 용으로 하나 DTO 만들어야 할 듯
     *
     */
    @Override
    public Page<MatchingPostResponse> searchMatchingPostsFromCondition(ArtistType artistType, WorkType workType,
                                                                       String keyword, Pageable pageable) {

        List<MatchingPostResponse> content =
                jpaQueryFactory.select(
                                Projections.constructor(
                                        MatchingPostResponse.class,
                                        matchingPost.id,
                                        matchingPost.title,
                                        matchingPost.description,
                                        matchingPost.artistType.stringValue(),
                                        matchingPost.workType.stringValue(),
                                        matchingPost.fileUrl,
                                        matchingPost.viewCount,
                                        matchingPost.createdAt,
                                        matchingPost.updatedAt
                                )
                        )
                        .from(matchingPost)
                        .where(
                                eqArtistType(artistType),
                                eqWorkType(workType),
                                containsKeyword(keyword)
                        )
                        .offset(pageable.getOffset())
                        .limit(pageable.getPageSize())
                        .orderBy(matchingPost.viewCount.desc())
                        .fetch();

        JPAQuery<Long> countQuery = jpaQueryFactory.select(matchingPost.count())
                .from(matchingPost)
                .where(
                        eqArtistType(artistType),
                        eqWorkType(workType),
                        containsKeyword(keyword)
                )
                .orderBy(matchingPost.viewCount.desc());

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression eqArtistType(ArtistType artistType) {
        return artistType != null ? matchingPost.artistType.eq(artistType) : null;
    }

    private BooleanExpression eqWorkType(WorkType workType) {
        return workType != null ? matchingPost.workType.eq(workType) : null;
    }

    private BooleanExpression containsKeyword(String keyword) {
        return keyword != null ?
                matchingPost.title.containsIgnoreCase(keyword)
                        .or(matchingPost.description.containsIgnoreCase(keyword))
                : null;
    }


}
