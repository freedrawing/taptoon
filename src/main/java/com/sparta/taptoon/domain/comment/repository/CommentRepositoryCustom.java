package com.sparta.taptoon.domain.comment.repository;

import com.sparta.taptoon.domain.comment.dto.response.CommentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentRepositoryCustom {

    Page<CommentResponse> findAllCommentsByMatchingPostId(Long matchingPostId, Pageable pageable);
    Page<CommentResponse> findAllRepliesByParentId(Long parentId, Pageable pageable);
}
