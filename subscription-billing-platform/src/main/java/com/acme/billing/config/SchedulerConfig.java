package com.acme.billing.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class SchedulerConfig {

    /**
     * Scheduler pool used to drive @Scheduled tasks. Sized above 1 so that the
     * nightly renewal job never blocks other scheduled housekeeping tasks (cache
     * eviction, health pings, etc.) that share the same scheduler.
     */
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);
        scheduler.setThreadNamePrefix("billing-scheduler-");
        scheduler.initialize();
        return scheduler;
    }
}
