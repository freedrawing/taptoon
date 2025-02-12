package com.sparta.taptoon.domain.matchingpost.repository;

import com.sparta.taptoon.domain.matchingpost.entity.MatchingPost;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MatchingPostRepository extends JpaRepository<MatchingPost, Long>, MatchingPostRepositoryCustom {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM MatchingPost m WHERE m.id = :matchingPostId")
    Optional<MatchingPost> findByIdWithLock(Long matchingPostId);
}
