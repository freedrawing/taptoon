package com.sparta.taptoon.domain.portfolio.entity;

import com.sparta.taptoon.domain.portfolio.enums.FileType;
import com.sparta.taptoon.global.common.BaseEntity;
import com.sparta.taptoon.global.common.enums.Status;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "portfolio_file")
public class PortfolioFile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false, updatable = false)
    private Portfolio portfolio;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    // fileType이 image면 썸네일 이미지 생성
    @Column(name = "thumbnail_url", length = 1000)
    private String thumbnailUrl;

    @Column(name = "file_url", nullable = false, length = 1000)
    private String fileUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false)
    private FileType fileType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Builder
    public PortfolioFile(Portfolio portfolio, String fileName, String thumbnailUrl, String fileUrl, String fileType) {
        this.portfolio = portfolio;
        this.fileName = fileName;
        this.thumbnailUrl = thumbnailUrl;
        this.fileUrl = fileUrl;
        this.fileType = FileType.of(fileType);
        this.status = Status.PENDING;
    }

    public void registerMe() {
        status = Status.REGISTERED;
        updateCreatedAtToNow();
    }

    public void removeFile() {
        this.status = Status.DELETING;
    }
}
