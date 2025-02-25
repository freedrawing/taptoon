package com.sparta.taptoon.domain.home.controller;

import com.sparta.taptoon.global.common.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public ResponseEntity<ApiResponse<Map<String, Object>>> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("success_or_fail", true);
        response.put("data", "Welcome to Taptoon API");
        response.put("message", "OK");

        return ApiResponse.success(response);
    }

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("success_or_fail", true);
        response.put("data", "서비스 정상 작동 중!");
        response.put("message", "OK");
        return ApiResponse.success(response);
    }
}
