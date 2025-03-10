package com.sparta.taptoon.domain.comment.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.taptoon.domain.comment.dto.response.CommentResponse;
import com.sparta.taptoon.domain.member.entity.QMember;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.Collections;
import java.util.List;

import static com.sparta.taptoon.domain.comment.entity.QComment.*;

@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepositoryCustom{

    private final JPAQueryFactory jpaQueryFactory;

    // 댓글 페이지네이션
    @Override
    public Page<CommentResponse> findAllCommentsByMatchingPostId(Long matchingPostId, Pageable pageable) {
        List<CommentResponse> comments = jpaQueryFactory
                .select(Projections.constructor(
                        CommentResponse.class,
                        comment.id,
                        comment.matchingPost.id,
                        comment.member.id,
                        comment.member.name, // Member의 name 필드 직접 조회
                        comment.parent.id,
                        comment.content,
                        comment.createdAt,
                        comment.updatedAt,
                        Expressions.constant(Collections.emptyList())
                        ))
                .from(comment)
                .leftJoin(comment.member, QMember.member)
                .where(comment.isDeleted.eq(false), // 삭제된 댓글 조회하지 않기
                        comment.matchingPost.id.eq(matchingPostId),
                        comment.parent.isNull()) // 부모가 없는 댓글 (댓글)
                .orderBy(comment.createdAt.desc()) // 최신순 정렬
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 댓글수 조회
        JPAQuery<Long> totalCount = jpaQueryFactory
                .select(comment.count())
                .from(comment)
                .where(comment.matchingPost.id.eq(matchingPostId),
                        comment.parent.isNull());

        return PageableExecutionUtils.getPage(comments, pageable, totalCount::fetchOne);
    }

    // 답글 페이지네이션
    @Override
    public Page<CommentResponse> findAllRepliesByParentId(Long parentId, Pageable pageable) {
        List<CommentResponse> replies = jpaQueryFactory
                .select(Projections.constructor(
                        CommentResponse.class,
                        comment.id,
                        comment.matchingPost.id,
                        comment.member.id,
                        comment.member.name, // Member의 name 필드 직접 조회
                        comment.parent.id,
                        comment.content,
                        comment.createdAt,
                        comment.updatedAt,
                        Expressions.constant(Collections.emptyList())
                ))
                .from(comment)
                .leftJoin(comment.member, QMember.member)
                .where(comment.isDeleted.eq(false), // 삭제된 댓글 조회하지 않기
                        comment.parent.id.eq(parentId))
                .orderBy(comment.createdAt.desc()) // 최신순 정렬
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> totalCount = jpaQueryFactory
                .select(comment.count())
                .from(comment)
                .where(comment.parent.id.eq(parentId));

        return PageableExecutionUtils.getPage(replies, pageable, totalCount::fetchOne);
    }
}
