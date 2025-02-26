package com.sparta.taptoon.domain.matchingpost.enums;

/**
 * 현재 `MatchingPost`가 활성화된 포스트인지 아닌지 구분할 때 사용됨. is_deleted랑 구분됨
 * 이 컬럼은 한 번 `REDISTERED`로 바뀌면 다시 생성되지 않는 컬럼임. 그래서 좀 아쉽기는 하다.
 */
public enum Status {
    PENDING, REGISTERED
}
