package com.sparta.taptoon.global.config;

import feign.Client;
import feign.codec.Decoder;
import feign.codec.Encoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {
    @Bean
    public Client feignClient() {
        return new Client.Default(null, null);
    }

    @Bean
    public Decoder feignDecoder() {
        return new Decoder.Default();
    }

    @Bean
    public Encoder feignEncoder() {
        return new Encoder.Default();
    }
}
