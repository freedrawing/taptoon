package com.sparta.taptoon.domain.comment.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sparta.taptoon.domain.comment.entity.Comment;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static com.sparta.taptoon.domain.comment.entity.QComment.*;

@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepositoryCustom{

    private final JPAQueryFactory jpaQueryFactory;

    // 댓글 페이지네이션
    @Override
    public Page<Comment> findAllCommentsByMatchingPostId(Long matchingPostId, Pageable pageable) {
        List<Comment> comments = jpaQueryFactory
                .selectFrom(comment)
                .where(comment.isDeleted.eq(false), // 삭제된 댓글 조회하지 않기
                        comment.matchingPost.id.eq(matchingPostId),
                        comment.parent.isNull()) // 부모가 없는 댓글 (댓글)
                .orderBy(comment.createdAt.asc()) // 최신순 정렬
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 댓글수 조회
        Long totalCount = jpaQueryFactory
                .select(comment.count())
                .from(comment)
                .where(comment.matchingPost.id.eq(matchingPostId),
                        comment.parent.isNull())
                .fetchOne();

        return new PageImpl<>(comments, pageable, totalCount);
    }

    // 답글 페이지네이션
    @Override
    public Page<Comment> findAllRepliesByParentId(Long parentId, Pageable pageable) {
        List<Comment> replies = jpaQueryFactory
                .selectFrom(comment)
                .where(comment.isDeleted.eq(false), // 삭제된 댓글 조회하지 않기
                        comment.parent.id.eq(parentId))
                .orderBy(comment.createdAt.asc()) // 최신순 정렬
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long totalCount = jpaQueryFactory
                .select(comment.count())
                .from(comment)
                .where(comment.parent.id.eq(parentId))
                .fetchOne();
        return new PageImpl<>(replies, pageable, totalCount);
    }
}
