package com.sparta.taptoon.domain.member.repository;

import com.sparta.taptoon.domain.category.enums.Genre;
import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.domain.member.enums.OAuthProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
    Long countAllByEmail(String email);
    Optional<Member> findByProviderAndProviderId(OAuthProvider provider, String providerId);
    Optional<Member>findByProviderId(String providerId);
    Page<Member> findByNameOrNickname(String name, String nickname, Pageable pageable);
}
