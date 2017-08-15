package com.indeed.util.core.time;

/**
 * Non-static interface to System.currentTimeMillis(), useful for testing.
 *
 * @author patrick@indeed.com
 */
public interface WallClock {
    long currentTimeMillis();

    long nanoTime();
}
