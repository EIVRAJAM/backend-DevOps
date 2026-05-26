// com/devops/backend/config/AsyncConfig.java

package com.devops.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Pool de hilos dedicado exclusivamente al envío de correos.
     */
    @Bean(name = "emailTaskExecutor")
    public Executor emailTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);      // 3 hilos siempre listos
        executor.setMaxPoolSize(10);      // crece hasta 10 hilos si hay mas tareas
        executor.setQueueCapacity(50);    // 50 correos en cola antes de rechazar
        executor.setThreadNamePrefix("email-worker-"); // visible en logs: email-worker-1
        executor.initialize();
        return executor;
    }
}