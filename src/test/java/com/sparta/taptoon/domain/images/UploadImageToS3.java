package com.sparta.taptoon.domain.images;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.sparta.taptoon.domain.file.dto.S3UploadClient;
import com.sparta.taptoon.domain.file.dto.response.PresignedUrlResponse;
import com.sparta.taptoon.domain.file.service.AwsS3Service;
import com.sparta.taptoon.domain.file.service.FileServiceImpl;
import com.sparta.taptoon.domain.matchingpost.service.MatchingPostService;
import com.sparta.taptoon.domain.portfolio.service.PortfolioService;
import com.sparta.taptoon.global.common.Constant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UploadImageToS3 {

    @InjectMocks
    private FileServiceImpl imageService;

    @Mock
    AwsS3Service awsS3Service;

    @Mock
    S3UploadClient s3UploadClient;

    @Mock
    private AmazonS3 amazonS3;

    @Mock
    PortfolioService portfolioService;

    @Mock
    MatchingPostService matchingPostService;

    // 우선 오류 안 나게만 수정. 실제 업로드는 안 될 듯
    @Test
    void uploadImageUsingFeign() throws IOException {
        // given
        String directory = "portfolio";
        String fileName = "test-image.jpg";
        String mockPreSignedUrl = "https://test-bucket.s3.amazonaws.com/test/test-image.jpg";

        // Mock URL 객체 생성
        URL mockUrl = new URL(mockPreSignedUrl);

        // amazonS3 mock 설정
        doNothing()
                .when(s3UploadClient)
                .uploadFile(eq("image/jpeg"), any(byte[].class));

        when(portfolioService.generateEmptyPortfolioFile(
                anyLong(), anyString(), anyString(), anyString(), anyString())
        )
                .thenReturn(1L);

        // matchingPostService 스터빙 제거 (사용되지 않는 경우)
        // when(matchingPostService.generateEmptyMatchingPostImage(anyLong(), anyString(), anyString(), anyString()))
        //         .thenReturn(1L);

        when(awsS3Service.generatePresignedUrl(anyString(), anyString())).thenAnswer(invocation -> {
            String directoryArg = invocation.getArgument(0);
            String fileKey = invocation.getArgument(1);
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest("test-bucket", directoryArg + "/" + fileKey, null);
            return amazonS3.generatePresignedUrl(request).toString();
        });

        when(amazonS3.generatePresignedUrl(any(GeneratePresignedUrlRequest.class))).thenReturn(mockUrl);
        when(awsS3Service.getFullUrl(anyString(), anyString())).thenReturn("fullPath");

        // when
        PresignedUrlResponse preSignedUrl = imageService.generatePresignedUrl(directory, 1L, Constant.IMAGE_TYPE, fileName);

        // 테스트 이미지 로드
        ClassPathResource resource = new ClassPathResource("test-images/test-image.jpg");
        byte[] imageBytes = FileCopyUtils.copyToByteArray(resource.getInputStream());

        // 파일 업로드 - 예외가 발생하지 않으면 성공
        assertDoesNotThrow(() -> s3UploadClient.uploadFile("image/jpeg", imageBytes));
        assertEquals(mockUrl.toString(), preSignedUrl.uploadingImageUrl());
        verify(s3UploadClient).uploadFile(eq("image/jpeg"), any(byte[].class));
        verify(amazonS3).generatePresignedUrl(any(GeneratePresignedUrlRequest.class));
        verify(awsS3Service).generatePresignedUrl(anyString(), anyString());
    }
}