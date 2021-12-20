package com.indeed.util.core.threads;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.Thread.UncaughtExceptionHandler;

public class LogOnUncaughtExceptionHandler implements UncaughtExceptionHandler {

    public static final Logger log = LoggerFactory.getLogger(LogOnUncaughtExceptionHandler.class);

    public LogOnUncaughtExceptionHandler() {
    }

    /**
     * @deprecated Use {@link #LogOnUncaughtExceptionHandler()}
     */
    @Deprecated
    public LogOnUncaughtExceptionHandler(final org.apache.log4j.Logger logger) {
        this();
    }

    @Override
    public void uncaughtException(final Thread t, final Throwable e) {
        log.error("Uncaught throwable in thread " + t.getName() + "/" + t.getId(), e);
    }
}