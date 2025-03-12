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

    @Modifying
    @Query("UPDATE PortfolioFile pf SET pf.status = :newStatus, pf.updatedAt = NOW() WHERE pf.id IN :ids")
    void updateStatusByIds(@Param("ids") List<Long> ids, @Param("newStatus") Status newStatus);

    List<PortfolioFile> findByStatusAndCreatedAtBefore(Status status, LocalDateTime createdAt);
}
