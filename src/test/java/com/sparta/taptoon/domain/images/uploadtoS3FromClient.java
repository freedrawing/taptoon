package com.sparta.taptoon.domain.images;

import com.sparta.taptoon.domain.image.S3UploadClient;
import com.sparta.taptoon.domain.image.service.ImageService;
import feign.Client;
import feign.Feign;
import feign.codec.Decoder;
import feign.codec.Encoder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@ActiveProfiles("test")
public class uploadtoS3FromClient {
    @Autowired
    private ImageService imageService;

    @DisabledIfSystemProperty(named = "CI", matches = "true")
    @Test
    void uploadImageUsingFeign() throws IOException {
        // given
        String directory_m = "matchingpost";
        String directory_c = "chat";
        String directory_p = "portfolio";
        String directory_t = "test";
        String directory = directory_t;
        String fileName = "test-image.jpg";

        String presignedUrl = imageService.generatePresignedUrl(directory, fileName);

        S3UploadClient s3UploadClient = Feign.builder()
                .contract(new SpringMvcContract())
                .client(new Client.Default(null, null))
                .encoder(new Encoder.Default())
                .decoder(new Decoder.Default())
                .target(S3UploadClient.class, presignedUrl);

        // 테스트 이미지 로드
        ClassPathResource resource = new ClassPathResource("test-images/test-image.jpg");
        byte[] imageBytes = FileCopyUtils.copyToByteArray(resource.getInputStream());

        // 파일 업로드 - 예외가 발생하지 않으면 성공
        assertDoesNotThrow(() -> s3UploadClient.uploadFile(
                "image/jpeg",
                imageBytes
        ));
    }
}
