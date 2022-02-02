package com.application.syncapp;

import java.util.concurrent.Executor;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AppAsyncConfig implements AsyncConfigurer {


	@Override
	public Executor getAsyncExecutor() {
		  ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		  taskExecutor.setCorePoolSize(40);
		  taskExecutor.setMaxPoolSize(40);
		  taskExecutor.setQueueCapacity(Integer.MAX_VALUE);
		  taskExecutor.setThreadNamePrefix("Aynsc-");
		  taskExecutor.initialize();
		  return taskExecutor;
	}

	@Override
	public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
		return new CustomAsyncExceptionHandler();
	}
}

