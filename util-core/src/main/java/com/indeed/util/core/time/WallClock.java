package com.indeed.util.core.time;

/**
 * Non-static interface to System.currentTimeMillis(), useful for testing.
 *
 * @deprecated Use {@link java.time.Clock} instead. This intent of this class is better served using
 *     the standardized API from the {@code java.time} package.
 *     <p>Replace calls to {@link #currentTimeMillis()} with {@link java.time.Clock#millis()}.
 * @author patrick@indeed.com
 */
@Deprecated
public interface WallClock {
    long currentTimeMillis();
}
