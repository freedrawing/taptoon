package com.sparta.taptoon.domain.matchingpost.repository;

import com.sparta.taptoon.domain.matchingpost.entity.MatchingPostImage;
import com.sparta.taptoon.global.common.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MatchingPostImageRepository extends JpaRepository<MatchingPostImage, Long> {

    @Modifying
    @Query("UPDATE MatchingPostImage m SET m.status = :newStatus, m.updatedAt = NOW() WHERE m.id IN :ids")
    void updateStatusByIds(@Param("ids") List<Long> ids, @Param("newStatus") Status newStatus);

    List<MatchingPostImage> findByStatusAndCreatedAtBefore(Status status, LocalDateTime createdAt);
}
