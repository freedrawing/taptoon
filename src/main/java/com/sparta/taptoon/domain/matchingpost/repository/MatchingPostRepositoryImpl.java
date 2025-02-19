package com.sparta.taptoon.domain.matchingpost.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.BooleanTemplate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.taptoon.domain.matchingpost.dto.response.MatchingPostResponse;
import com.sparta.taptoon.domain.matchingpost.enums.ArtistType;
import com.sparta.taptoon.domain.matchingpost.enums.WorkType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.sparta.taptoon.domain.matchingpost.entity.QMatchingPost.matchingPost;

@RequiredArgsConstructor
public class MatchingPostRepositoryImpl implements MatchingPostRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<MatchingPostResponse> searchMatchingPostsFromCondition(ArtistType artistType, WorkType workType,
                                                                       List<Long> ids, Pageable pageable) {

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
                                matchingPost.id.in(ids), // ES로부터 받아온 값
                                matchingPost.isDeleted.isFalse()
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
                        matchingPost.id.in(ids),
                        matchingPost.isDeleted.isFalse()
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



}
