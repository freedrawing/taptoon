package com.sparta.taptoon.domain.matchingpost.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MatchingPostRepositoryImpl implements MatchingPostRepositoryCustom{

    private final JPAQueryFactory jpaQueryFactory;
}
