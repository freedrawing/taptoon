package com.sparta.taptoon.domain.auth.dto;

import com.sparta.taptoon.domain.auth.dto.response.OAuthApiResponse;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OAuthMemberInfo {
    private String id;
    private String name;
    private String email;

    public static OAuthMemberInfo from(OAuthApiResponse.Response response) {
        return OAuthMemberInfo.builder()
                .id(response.getProviderId())
                .name(response.name())
                .build();
    }
}
