package com.sparta.taptoon.domain.comment.repository;

import com.sparta.taptoon.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByMatchingPostIdOrderByCreatedAt(Long matchingPostId);

    List<Comment> findAllById(Long commentId);
}
