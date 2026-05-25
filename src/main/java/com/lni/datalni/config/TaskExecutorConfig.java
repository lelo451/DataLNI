package com.lni.datalni.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutor;

import java.util.concurrent.Executor;

/**
 * Backs the JavaFX {@code Task}s that run service/DB calls off the FX Application Thread.
 * Wrapped in a {@link DelegatingSecurityContextExecutor} so the logged-in user's
 * {@code SecurityContext} propagates to worker threads and {@code @PreAuthorize} sees it.
 */
@Configuration
public class TaskExecutorConfig {

    public static final String FX_EXECUTOR = "fxTaskExecutor";

    /**
     * The underlying pool, exposed as a managed bean so Spring shuts it down on context
     * close. Threads are daemons so they never keep the JVM alive after the window closes.
     */
    @Bean
    public ThreadPoolTaskExecutor fxThreadPool() {
        ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
        pool.setCorePoolSize(2);
        pool.setMaxPoolSize(4);
        pool.setQueueCapacity(50);
        pool.setThreadNamePrefix("fx-task-");
        pool.setDaemon(true);
        pool.initialize();
        return pool;
    }

    @Bean(name = FX_EXECUTOR)
    public Executor fxTaskExecutor(ThreadPoolTaskExecutor fxThreadPool) {
        return new DelegatingSecurityContextExecutor(fxThreadPool);
    }
}
