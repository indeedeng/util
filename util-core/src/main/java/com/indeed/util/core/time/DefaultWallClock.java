package com.indeed.util.core.time;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Default {@link WallClock} implementation using system current time.
 *
 * @deprecated with {@link WallClock}. Use an instance of {@link java.time.Clock} instead.
 *
 * @author patrick@indeed.com
 */
@ThreadSafe
public class DefaultWallClock implements WallClock {
    @Override
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }
}
