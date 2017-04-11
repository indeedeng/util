package com.indeed.util.core.time;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Simple wall clock frozen to a moment in time.
 *
 * @author matts
 */
public final class StoppedClock implements WallClock {
    @Nonnull private final AtomicLong millis;

    /**
     * Creates a new stopped clock frozen at the current moment in time.
     */
    public StoppedClock() {
        this(System.currentTimeMillis());
    }

    /**
     * Creates a new stopped clock frozen at the given moment in time.
     */
    public StoppedClock(final long millis) {
        this.millis = new AtomicLong(millis);
    }

    /**
     * Reset this stopped clock to the given moment in time.
     */
    public final void set(final long millis) {
        this.millis.set(millis);
    }

    /**
     * Reset this stopped clock to a moment in time relative to its current frozen value.
     */
    public final long plus(final long value, @Nonnull final TimeUnit timeUnit) {
        return this.millis.addAndGet(timeUnit.toMillis(value));
    }

    @Override
    public long currentTimeMillis () {
        return millis.get();
    }
}
