package com.application.syncapp;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

public class CustomAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(CustomAsyncExceptionHandler.class);

	@Override
	public void handleUncaughtException(Throwable ex, Method method, Object... params) {
		// LOG.error("Exception while executing with message {} ", throwable.getMessage());
		LOG.error("Exception while executing with message {} ", ex.getMessage());
        LOG.error("Exception happen in {} method ", method.getName());
		
	}
}