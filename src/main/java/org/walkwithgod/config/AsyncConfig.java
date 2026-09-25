package org.walkwithgod.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig {

    /**
     * The primary async executor used by @Async methods.
     * Named "taskExecutor" so Spring picks it unambiguously
     * even when other TaskExecutors exist (e.g. WebSocket channels).
     */
    @Bean(name = "taskExecutor")
    @Primary
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(4);
        ex.setMaxPoolSize(12);
        ex.setQueueCapacity(200);
        ex.setThreadNamePrefix("wwg-async-");
        ex.initialize();
        return ex;
    }
}