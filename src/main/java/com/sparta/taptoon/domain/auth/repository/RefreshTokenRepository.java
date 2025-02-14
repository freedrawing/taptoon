package com.sparta.taptoon.domain.auth.repository;

import com.sparta.taptoon.domain.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteAllByMemberId(Long memberId);
    void deleteByMemberIdAndDeviceInfo(Long memberId, String deviceInfo);
}
