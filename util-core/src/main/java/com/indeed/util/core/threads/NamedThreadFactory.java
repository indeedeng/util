package com.indeed.util.core.threads;

import java.util.concurrent.ThreadFactory;

/**
 * ThreadFactory implementation that creates threads with descriptive names.
 * Helpful when creating thread pools using the Executor class.
 */
public class NamedThreadFactory implements ThreadFactory {
    private final String threadNamePrefix;
    private final String threadName;
    private final boolean isDaemon;
    private int threadCount = 0;

    public NamedThreadFactory(final String threadName) {
        this(threadName, false);
    }

    public NamedThreadFactory(final String threadName, boolean isDaemon) {
        this.threadName = threadName;
        this.threadNamePrefix = threadName + "-Thread-";
        this.isDaemon = isDaemon;
    }

    /**
     * @deprecated Use {@link #NamedThreadFactory(java.lang.String)}
     */
    @Deprecated
    public NamedThreadFactory(final String threadName, String loggerName) {
        this(threadName);
    }

    /**
     * @deprecated Use {@link #NamedThreadFactory(java.lang.String)}
     */
    @Deprecated
    public NamedThreadFactory(final String threadName, final org.apache.log4j.Logger logger) {
        this(threadName);
    }

    /**
     * @deprecated Use {@link #NamedThreadFactory(java.lang.String, boolean)}
     */
    @Deprecated
    public NamedThreadFactory(final String threadName, boolean isDaemon, final org.apache.log4j.Logger logger) {
        this(threadName, isDaemon);
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
        t.setUncaughtExceptionHandler(new LogOnUncaughtExceptionHandler());
        return t;
    }
}
