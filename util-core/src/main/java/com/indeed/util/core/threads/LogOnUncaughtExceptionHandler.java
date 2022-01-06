package com.indeed.util.core.threads;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.Thread.UncaughtExceptionHandler;

/**
 * @deprecated Use a lambda to log the error yourself. Due to log4j1 removal, this implementation
 * no longer preserves the class name from the logger passed in.
 */
@Deprecated
public class LogOnUncaughtExceptionHandler implements UncaughtExceptionHandler {

    public static final Logger log = LoggerFactory.getLogger(LogOnUncaughtExceptionHandler.class);

    /**
     * @deprecated Use a lambda to log the error yourself. Due to log4j1 removal, this implementation
     * no longer preserves the class name from the logger passed in.
     */
    @Deprecated
    public LogOnUncaughtExceptionHandler() {
    }

    /**
     * @deprecated Use a lambda to log the error yourself. Due to log4j1 removal, this implementation
     * no longer preserves the class name from the logger passed in.
     */
    @Deprecated
    public LogOnUncaughtExceptionHandler(final org.apache.log4j.Logger logger) {
    }

    @Override
    public void uncaughtException(final Thread t, final Throwable e) {
        log.error("Uncaught throwable in thread " + t.getName() + "/" + t.getId(), e);
    }
}