package com.sparta.taptoon.domain.image.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PreSignedUrlRequest(
        @NotBlank(message = "폴더 이름은 필수입니다.") @Pattern(regexp = "^[a-zA-Z0-9/._-]+$", message = "올바르지 않은 폴더 이름입니다.") String directory,
        @NotBlank(message = "id 값은 필수입니다.") @Pattern(regexp = "^[a-zA-Z0-9/._-]+$", message = "올바르지 않은 폴더 이름입니다.") Long id,
        @NotBlank(message = "파일 이름은 필수입니다.") @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "올바르지 않은 파일 이름입니다.") String fileName) {
}
