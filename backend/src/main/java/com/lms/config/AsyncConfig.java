package com.lms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Ref: SRS 2.11, 14.15 - "Email delivery shall occur asynchronously" and
 * "Business operations shall never depend on successful email delivery."
 * Named executor used by @Async("notificationExecutor") methods in the
 * notification module so a slow/failed SMTP call never blocks the request
 * thread that triggered it (payment success, enrollment, etc.).
 */
@Configuration
public class AsyncConfig {

    @Bean(name = "notificationExecutor")
    public Executor notificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("notification-");
        executor.initialize();
        return executor;
    }

    /**
     * Ref: SRS 15.15 - large report exports render off the request thread.
     * Kept separate from the notification pool on purpose: an export is slow
     * and memory-hungry, and a burst of them must not starve the email queue
     * that password resets and payment receipts depend on. The pool is
     * deliberately small for the same reason - a handful of concurrent
     * renders is plenty, and queueing the rest is better than running twenty
     * at once.
     */
    @Bean(name = "reportExecutor")
    public Executor reportExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(3);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("report-export-");
        executor.initialize();
        return executor;
    }
}
