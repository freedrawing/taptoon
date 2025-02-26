package com.sparta.taptoon.domain.portfolio.entity;

import com.sparta.taptoon.global.common.BaseEntity;
import com.sparta.taptoon.global.common.enums.ImageStatus;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "portfolio_image")
public class PortfolioImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "image_url", nullable = false, length = 1000)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false, updatable = false)
    private Portfolio portfolio;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ImageStatus status;

    @Builder
    public PortfolioImage(String imageUrl, Portfolio portfolio, ImageStatus status) {
        this.imageUrl = imageUrl;
        this.portfolio = portfolio;
        this.status = status;
    }
}
