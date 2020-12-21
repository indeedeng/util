package com.indeed.util.core.time;

import java.time.Instant;
import java.time.ZoneId;
import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Simple wall clock frozen to a moment in time.
 *
 * @deprecated with {@link WallClock}. Use {@link java.time.Clock#fixed(Instant, ZoneId)} instead.
 *
 * @author matts
 */
@Deprecated
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
     *
     * @param millis The current time to use for the stopped clock.
     */
    public StoppedClock(final long millis) {
        this.millis = new AtomicLong(millis);
    }

    /**
     * Reset this stopped clock to the given moment in time.
     *
     * @param millis The new time to set the stopped clock to.
     */
    public final void set(final long millis) {
        this.millis.set(millis);
    }

    /**
     * Add the specified amount of time to the current clock.
     *
     * @param value The numeric value to add to the clock after converting
     *              based on the provided {@code timeUnit}.
     * @param timeUnit The time unit that {@code value} is measured in.
     * @return The time after being adjusted by the provided offset.
     */
    public final long plus(final long value, @Nonnull final TimeUnit timeUnit) {
        return this.millis.addAndGet(timeUnit.toMillis(value));
    }

    @Override
    public long currentTimeMillis () {
        return millis.get();
    }
}
