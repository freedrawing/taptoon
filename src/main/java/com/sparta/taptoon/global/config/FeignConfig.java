package com.sparta.taptoon.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Client;
import feign.Logger;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class FeignConfig {
    private final ObjectMapper objectMapper;

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public Client feignClient() {
        return new Client.Default(null, null);
    }

    @Bean
    public Decoder feignDecoder() {
        return new JacksonDecoder(objectMapper);
    }

    @Bean
    public Encoder feignEncoder() {
        return new JacksonEncoder(objectMapper);
    }
}
