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
    @Nonnull private final AtomicLong nanos;

    /**
     * Creates a new stopped clock frozen at the current moment in time.
     */
    public StoppedClock() {
        this(System.nanoTime(), TimeUnit.NANOSECONDS);
    }

    /**
     * Creates a new stopped clock frozen at the given moment in time.
     *
     * @param millis The current time to use for the stopped clock.
     */
    public StoppedClock(final long millis) {
        this(millis, TimeUnit.MILLISECONDS);
    }

    /**
     * Creates a new stopped clock frozen at the given moment in time with the
     * specified time unit.
     *
     * @param time The time to use for the stopped clock.
     * @param timeUnit The time unit that {@code time} is measured in.
     */
    public StoppedClock(final long time, final TimeUnit timeUnit) {
        nanos = new AtomicLong(timeUnit.toNanos(time));
    }

    /**
     * Reset this stopped clock to the given moment in time.
     *
     * @param millis The new time to set the stopped clock to.
     */
    public final void set(final long millis) {
        set(millis, TimeUnit.MILLISECONDS);
    }

    /**
     * Reset this stopped clock to the given moment in time.
     *
     * @param time The new time to set the stopped clock to.
     * @param timeUnit The time unit that {@code time} is measured in.
     */
    public final void set(final long time, final TimeUnit timeUnit) {
        this.nanos.set(timeUnit.toNanos(time));
    }

    /**
     * Add the specified amount of time to the current clock.
     *
     * @param time The numeric value to add to the clock after converting
     *              based on the provided {@code timeUnit}.
     * @param timeUnit The time unit that {@code time} is measured in.
     * @return The time after being adjusted by the provided offset.
     */
    public final long plus(final long time, @Nonnull final TimeUnit timeUnit) {
        return this.nanos.addAndGet(timeUnit.toNanos(time));
    }

    @Override
    public long currentTimeMillis () {
        return TimeUnit.NANOSECONDS.toMillis(nanoTime());
    }

    @Override
    public long nanoTime() {
        return nanos.get();
    }
}
