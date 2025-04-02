package com.sparta.taptoon.domain.file.dto;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "s3-upload", url = "placeholder")  // URL은 동적으로 변경될 것이므로 placeholder
public interface S3UploadClient {
    @PutMapping
    void uploadFile(
            @RequestHeader("Content-Type") String contentType,
            @RequestBody byte[] file
    );
}
