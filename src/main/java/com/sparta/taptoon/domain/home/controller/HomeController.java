package com.sparta.taptoon.domain.home.controller;

import com.sparta.taptoon.global.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public ResponseEntity<ApiResponse<Map<String, Object>>> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("data", "Taptoon 홈페이지입니다.");

        return ApiResponse.success(response);
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("data", "서비스 정상 작동 중!");
        return ApiResponse.success(response);
    }

    @GetMapping("/login")
    public ResponseEntity<Void> redirectToFrontend() {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("https://taptoon.site/login"))
                .build();
    }
}