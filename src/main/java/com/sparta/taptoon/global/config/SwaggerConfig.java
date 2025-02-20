package com.sparta.taptoon.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .title("프로젝트 API Document")
                .version("v1.0")
                .description("프로젝트 API 명세서입니다.");

        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");
        // Security 요청 설정
        SecurityRequirement securityRequirement = new SecurityRequirement().addList("bearer-key");

        return new OpenAPI()
                .info(info)
                .components(new Components()
                        .addSecuritySchemes("bearer-key", securityScheme))
                .addSecurityItem(securityRequirement);
    }
}
