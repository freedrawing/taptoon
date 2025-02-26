package com.sparta.taptoon.domain.image.controller;

import com.sparta.taptoon.domain.image.dto.request.PreSignedUrlRequest;
import com.sparta.taptoon.domain.image.dto.response.PresignedUrlResponse;
import com.sparta.taptoon.domain.image.service.ImageService;
import com.sparta.taptoon.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "images", description = "이미지 업로드 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/images")
public class ImageController {
    private final ImageService imageService;

    @Operation(summary = "PresignedUrl 방식 이미지 업로드")
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<PresignedUrlResponse>> getPresignedUrl(@Valid @RequestBody PreSignedUrlRequest request) {

        PresignedUrlResponse presignedUrlResponse
                = imageService.generatePresignedUrl(request.directory(), request.id(), request.fileName());
        return ApiResponse.success(presignedUrlResponse);
    }

    @Operation(summary = "S3에서 이미지 삭제")
    @PostMapping("/delete")
    public ResponseEntity<ApiResponse<Void>> deleteImage(@Valid String imageUrl) {
        imageService.removeImageFromS3(imageUrl);
        return ApiResponse.noContent();
    }
}
