package com.sparta.taptoon.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NaverMemberInfo {
    private String id;
    private String name;
    private String email;
}
