package com.ga.JNews.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutorService;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@EnableAsync
@Configuration
public class AsyncConfiguration {

    /**
     * Asynchronous task executor that delegates security context.
     * @return Executor
     */
    @Bean
    public Executor executor() {
        return new DelegatingSecurityContextExecutorService(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
    }
}
