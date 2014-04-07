package com.indeed.util.core.threads;

import com.indeed.util.core.threads.LogOnUncaughtExceptionHandler;

import java.util.concurrent.ThreadFactory;

import org.apache.log4j.Logger;

/**
 * ThreadFactory implementation that creates threads with descriptive names.
 * Helpful when creating thread pools using the Executor class.
 */
public class NamedThreadFactory implements ThreadFactory {
    private final String threadNamePrefix;
    private final String threadName;
    private final boolean isDaemon;
    private int threadCount = 0;
    private final Logger logger;

    public NamedThreadFactory(final String threadName) {
        this(threadName, false);
    }

    public NamedThreadFactory(final String threadName, boolean isDaemon) {
        this(threadName, isDaemon, null);
    }

    public NamedThreadFactory(final String threadName, String loggerName){
        this(threadName, false, Logger.getLogger(loggerName));
    }

    public NamedThreadFactory(final String threadName, final Logger logger) {
        this(threadName, false, logger);
    }

    public NamedThreadFactory(final String threadName, boolean isDaemon, final Logger logger) {
        this.threadName = threadName;
        this.threadNamePrefix = threadName + "-Thread-";
        this.isDaemon = isDaemon;
        this.logger = logger;
    }

    public String getThreadName() {
        return threadName;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r, threadNamePrefix + threadCount++);
        if (isDaemon) {
            t.setDaemon(true);
        }
        t.setUncaughtExceptionHandler(new LogOnUncaughtExceptionHandler(getLogger(r)));
        return t;
    }

    private Logger getLogger(final Runnable r) {
        if (logger != null) {
            return logger;
        }
        final Class<? extends Runnable> runnableClass = r.getClass();
        if (runnableClass.getPackage().getName().startsWith("com.indeed.")) {
            return Logger.getLogger(runnableClass);
        }
        return Logger.getLogger(NamedThreadFactory.class);
    }
}
