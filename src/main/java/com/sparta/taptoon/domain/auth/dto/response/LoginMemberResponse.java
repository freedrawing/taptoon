package com.sparta.taptoon.domain.auth.dto.response;

import java.time.LocalDateTime;

public record LoginMemberResponse(String accessToken, String refreshToken, LocalDateTime tokenExpiresAt) {
}
