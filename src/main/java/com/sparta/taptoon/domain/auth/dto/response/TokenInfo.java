package com.sparta.taptoon.domain.auth.dto.response;

import java.time.LocalDateTime;

public record TokenInfo(String token, LocalDateTime expiresAt) {
}
