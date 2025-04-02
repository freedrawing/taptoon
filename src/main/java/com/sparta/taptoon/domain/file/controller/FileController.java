package com.sparta.taptoon.domain.file.controller;

import com.sparta.taptoon.domain.file.dto.request.PreSignedUrlRequest;
import com.sparta.taptoon.domain.file.dto.response.PresignedUrlResponse;
import com.sparta.taptoon.domain.file.service.FileService;
import com.sparta.taptoon.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "images", description = "이미지 업로드 API")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/images")
public class FileController {
    private final FileService fileService;

    @Operation(summary = "PresignedUrl 방식 이미지 업로드")
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<PresignedUrlResponse>> getPresignedUrl(@Valid @RequestBody PreSignedUrlRequest request) {

        PresignedUrlResponse presignedUrlResponse
                = fileService.generatePresignedUrl(request.directory(), request.id(), request.fileType(), request.fileName());
        log.info("preSignedUrl: {}",presignedUrlResponse.uploadingImageUrl());
        return ApiResponse.success(presignedUrlResponse);
    }
}
