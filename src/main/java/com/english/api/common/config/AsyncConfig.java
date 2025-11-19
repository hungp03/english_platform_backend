package com.english.api.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig implements AsyncConfigurer {

    @Bean(name = "taskExecutor")
    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Core pool size: number of threads to keep alive
        executor.setCorePoolSize(5);
        
        // Max pool size: maximum number of threads
        executor.setMaxPoolSize(10);
        
        // Queue capacity: number of tasks to queue before rejecting
        executor.setQueueCapacity(100);
        
        // Thread name prefix for easier debugging
        executor.setThreadNamePrefix("async-");
        
        // Wait for tasks to complete on shutdown
        executor.setWaitForTasksToCompleteOnShutdown(true);
        
        // Maximum time to wait for tasks on shutdown (seconds)
        executor.setAwaitTerminationSeconds(60);
        
        executor.initialize();
        return executor;
    }
}
