package com.lms.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Serves an interactive springdoc UI at /swagger-ui.html for exploring the
 * live backend. This is a convenience for development; the hand-authored
 * openapi.yaml (Ref: chapter-by-chapter API contract, project root) remains
 * the source-of-truth contract that frontend/backend both build against -
 * keep this bearer scheme and springdoc's generated paths consistent with it.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI lmsOpenApi() {
        final String bearerScheme = "bearerAuth";
        return new OpenAPI()
                .info(new Info().title("Dhyan Mitra LMS API").version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList(bearerScheme))
                .components(new Components().addSecuritySchemes(bearerScheme,
                        new SecurityScheme()
                                .name(bearerScheme)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
