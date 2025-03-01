package com.sparta.taptoon.domain.images;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.sparta.taptoon.domain.image.S3UploadClient;
import com.sparta.taptoon.domain.image.dto.response.PresignedUrlResponse;
import com.sparta.taptoon.domain.image.service.ImageServiceImpl;
import com.sparta.taptoon.domain.member.entity.Member;
import com.sparta.taptoon.domain.portfolio.entity.Portfolio;
import com.sparta.taptoon.domain.portfolio.entity.PortfolioImage;
import com.sparta.taptoon.domain.portfolio.repository.PortfolioImageRepository;
import com.sparta.taptoon.domain.portfolio.repository.PortfolioRepository;
import com.sparta.taptoon.global.common.enums.Status;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UploadImageToS3 {

    @InjectMocks
    private ImageServiceImpl imageService;
    
    @Mock
    S3UploadClient s3UploadClient;

    @Mock
    private AmazonS3 amazonS3;

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private PortfolioImageRepository portfolioImageRepository;

    @Test
    void uploadImageUsingFeign() throws IOException {
        // given
        String directory_m = "matchingpost";
        String directory_c = "chat";
        String directory_p = "portfolio";
        String directory_t = "test";
        String directory = directory_p;
        String fileName = "test-image.jpg";
        String mockPreSignedUrl = "https://test-bucket.s3.amazonaws.com/test/test-image.jpg";

        // Mock URL 객체 생성
        URL mockUrl = new URL(mockPreSignedUrl);

        // amazonS3 mock 설정
        doNothing()
                .when(s3UploadClient)
                .uploadFile(eq("image/jpeg"), any(byte[].class));
        Portfolio portfolio = Portfolio.builder()
                .member(new Member())
                .title("title")
                .content("content")
                .build();
        when(portfolioRepository.findById(any())).thenReturn(Optional.of(portfolio));
        PortfolioImage portfolioImage = PortfolioImage.builder()
                .portfolio(portfolio)
                .fileUrl("https://test-bucket.s3.amazonaws.com/test/test-image.jpg") // 실제 URL은 나중에 설정됨
                .status(Status.PENDING)
                .build();
        when(portfolioImageRepository.save(any())).thenReturn(portfolioImage);
        when(amazonS3.generatePresignedUrl(any())).thenReturn(mockUrl);
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
