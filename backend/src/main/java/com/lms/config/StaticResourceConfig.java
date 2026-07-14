package com.lms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Serves files written by LocalFileStorageService (profile photos, etc.) at /uploads/**. */
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    private final String localPath;

    public StaticResourceConfig(@Value("${app.storage.local-path}") String localPath) {
        this.localPath = localPath;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Only the public/ subtree is ever served statically - private/
        // (lesson resources, etc.) is reachable exclusively through the
        // signed-URL download endpoint (Ref: SRS 8.8, 8.15, 17.24).
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + localPath + "/public/");
    }
}
