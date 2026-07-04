package com.springweb.config.threads;

import org.apache.coyote.ProtocolHandler;
import org.springframework.boot.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executors;

@Configuration
public class ThreadConfig {

    @Bean
    public AsyncTaskExecutor applicationTaskExecutor() {
        return new TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor());
    }

    @Bean
    public TomcatProtocolHandlerCustomizer<ProtocolHandler> protocolHandlerVirtualThreadExecutorCustomizer() {
        return protocolHandler -> {
            System.out.println("🚀 Configuring Tomcat to use Virtual Threads");
            var executor = Executors.newVirtualThreadPerTaskExecutor();
            protocolHandler.setExecutor(executor);
            System.out.println("✅ Virtual Thread Executor configured");
        };
    }
}
