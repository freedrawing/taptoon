package com.sparta.taptoon.domain.member.repository;

import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.domain.member.enums.OAuthProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
    Optional<Member> findByProviderAndProviderId(OAuthProvider provider, String providerId);
    Optional<Member>findByProviderId(String providerId);
    Page<Member> findByNameOrNickname(String name, String nickname, Pageable pageable);
}
