package com.indeed.util.core.time;

/**
 * Default {@link WallClock} implementation using system current time.
 *
 * @deprecated with {@link WallClock}. Use an instance of {@link java.time.Clock} instead.
 * @author patrick@indeed.com
 *     <p>ThreadSafe
 */
public class DefaultWallClock implements WallClock {
    @Override
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }
}
