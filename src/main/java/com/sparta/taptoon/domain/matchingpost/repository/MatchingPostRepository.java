package com.sparta.taptoon.domain.matchingpost.repository;

import com.sparta.taptoon.domain.matchingpost.entity.MatchingPost;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MatchingPostRepository extends JpaRepository<MatchingPost, Long> {

    @Query("SELECT mp FROM MatchingPost mp JOIN FETCH mp.author WHERE mp.id = :matchingPostId")
    Optional<MatchingPost> findByIdWithAuthor(@Param("matchingPostId") Long matchingPostId);

}
