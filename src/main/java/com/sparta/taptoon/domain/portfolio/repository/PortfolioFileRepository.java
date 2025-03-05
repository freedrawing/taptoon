package com.sparta.taptoon.domain.portfolio.repository;

import com.sparta.taptoon.domain.portfolio.entity.PortfolioFile;
import com.sparta.taptoon.global.common.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

public interface PortfolioFileRepository extends JpaRepository<PortfolioFile, Long> {

    /*
     * 사용용도
     * 1. 포스트와 함께 이미지 등록할 때, REGISTERED로 변경할 때,
     * 2. 포스트 편집할 때, 기존에 업로드된 이미지 삭제할 때
     */
    @Modifying
    @Query("UPDATE PortfolioFile pf SET pf.status = :newStatus, pf.updatedAt = NOW() WHERE pf.id IN :ids")
    void updateStatusByIds(@Param("ids") List<Long> ids, @Param("newStatus") Status newStatus);
    List<PortfolioFile> findByStatusAndCreatedAtBefore(Status status, LocalDateTime createdAt);
}
