package com.sparta.taptoon.domain.auth.service;

import com.sparta.taptoon.domain.auth.dto.OAuthMemberInfo;
import com.sparta.taptoon.domain.member.enums.OAuthProvider;

public interface OAuthProviderService {
    String getAuthorizationUrl();
    String getAccessToken(String code, String state);
    OAuthMemberInfo getMemberInfo(String accessToken);
    OAuthProvider getProvider();
}
