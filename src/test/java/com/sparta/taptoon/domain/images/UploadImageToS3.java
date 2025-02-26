package com.sparta.taptoon.domain.images;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.sparta.taptoon.domain.image.S3UploadClient;
import com.sparta.taptoon.domain.image.dto.response.PresignedUrlResponse;
import com.sparta.taptoon.domain.image.service.ImageService;
import com.sparta.taptoon.domain.image.service.ImageServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class uploadImageToS3 {
    @InjectMocks
    private ImageServiceImpl imageService;

    @Mock
    S3UploadClient s3UploadClient;

    @Mock
    private AmazonS3 amazonS3;

    @Test
    void uploadImageUsingFeign() throws IOException {
        // given
        String directory_m = "matchingpost";
        String directory_c = "chat";
        String directory_p = "portfolio";
        String directory_t = "test";
        String directory = directory_t;
        String fileName = "test-image.jpg";
        String mockPreSignedUrl = "https://test-bucket.s3.amazonaws.com/test/test-image.jpg";

        // Mock URL 객체 생성
        URL mockUrl = new URL(mockPreSignedUrl);

        // amazonS3 mock 설정
        when(amazonS3.generatePresignedUrl(any())).thenReturn(mockUrl);
        doNothing()
                .when(s3UploadClient)
                .uploadFile(eq("image/jpeg"), any(byte[].class));
        //when
        PresignedUrlResponse preSignedUrl = imageService.generatePresignedUrl(directory,1L, fileName);

        // 테스트 이미지 로드
        ClassPathResource resource = new ClassPathResource("test-images/test-image.jpg");
        byte[] imageBytes = FileCopyUtils.copyToByteArray(resource.getInputStream());

        // 파일 업로드 - 예외가 발생하지 않으면 성공
        assertDoesNotThrow(() -> s3UploadClient.uploadFile("image/jpeg", imageBytes));
        assertEquals(mockUrl.toString(), preSignedUrl.uploadingImageUrl());
        verify(s3UploadClient).uploadFile(eq("image/jpeg"), any(byte[].class));
        verify(amazonS3).generatePresignedUrl(any(GeneratePresignedUrlRequest.class));
    }
}
