package com.indeed.util.core;

import java.lang.Thread.UncaughtExceptionHandler;

import org.apache.log4j.Logger;

public class LogOnUncaughtExceptionHandler implements UncaughtExceptionHandler {
    private final Logger logger;

    public LogOnUncaughtExceptionHandler(final Logger logger) {
        this.logger = logger;
    }

    @Override
    public void uncaughtException(final Thread t, final Throwable e) {
        logger.error("Uncaught throwable in thread " + t.getName() + "/" + t.getId(), e);
    }
}