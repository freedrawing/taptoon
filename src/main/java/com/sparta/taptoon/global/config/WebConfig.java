package com.sparta.taptoon.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());// Java 8의 날짜/시간 타입(LocalDateTime 등)을 처리할 수 있는 모듈 등록
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);// 날짜를 타임스탬프가 아닌 ISO-8601 형식의 문자열로 직렬화하도록 설정
        // 위에서 설정한 ObjectMapper를 사용하는 새로운 HTTP 메시지 변환기를 생성
        MappingJackson2HttpMessageConverter converter =
                new MappingJackson2HttpMessageConverter(objectMapper);
        converters.add(0, converter);// 생성한 변환기를 변환기 목록의 가장 앞에 추가
    }
}
