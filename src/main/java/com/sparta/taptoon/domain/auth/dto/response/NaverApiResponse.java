package com.sparta.taptoon.domain.auth.dto.response;

import lombok.Getter;

@Getter
public class NaverApiResponse {
    private Response response;

    @Getter
    public static class Response {
        private String id;
        private String name;
    }
}
