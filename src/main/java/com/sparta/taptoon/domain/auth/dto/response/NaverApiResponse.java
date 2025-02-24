package com.sparta.taptoon.domain.auth.dto.response;

import lombok.Getter;

public record NaverApiResponse(Response response) {
    public record Response(String id, String name) {}
}
