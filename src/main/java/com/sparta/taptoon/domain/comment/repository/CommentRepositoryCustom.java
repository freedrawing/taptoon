package com.sparta.taptoon.domain.comment.repository;

import com.sparta.taptoon.domain.comment.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentRepositoryCustom {

    Page<Comment> findAllCommentsByMatchingPostId(Long matchingPostId, Pageable pageable);
    Page<Comment> findAllRepliesByParentId(Long parentId, Pageable pageable);
}
