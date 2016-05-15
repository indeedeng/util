package com.indeed.util.core.time;

import javax.annotation.concurrent.ThreadSafe;

/**
 * @author patrick@indeed.com
 */
@ThreadSafe
public class DefaultWallClock implements WallClock {
    @Override
    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }
}
