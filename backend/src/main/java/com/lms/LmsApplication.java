package com.lms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Ref: SRS Chapter 2 - Monolithic architecture with a feature-based modular
 * structure. Deployed as a single executable; internal packages under
 * com.lms are organized per business module (see package-info.java in each).
 */
@SpringBootApplication
@EnableAsync // Ref: SRS 2.11 - notification/email delivery must be asynchronous
@ConfigurationPropertiesScan // picks up JwtProperties and any future @ConfigurationProperties classes
public class LmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(LmsApplication.class, args);
    }
}
