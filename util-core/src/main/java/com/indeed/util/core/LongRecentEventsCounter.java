package com.indeed.util.core;

import com.google.common.primitives.Longs;

import java.util.Arrays;
import java.util.Collections;

/**
 * Requires external synchronization
 *
 * @author ketan
 */
public class LongRecentEventsCounter {
    private final long[] buckets;
    private final Ticker ticker;
    private int indexOfOldest;
    private int tickOfOldest;

    /**
     * @param ticker size of a time interval
     * @param size number of intervals to record
     */
    public LongRecentEventsCounter(final Ticker ticker, final int size) {
        if (size < 2) {
            throw new IllegalArgumentException(
                    "Size " + size + " is too small; must be at least 2");
        }
        this.ticker = ticker;
        this.buckets = new long[size];
        this.indexOfOldest = 1;
        this.tickOfOldest = ticker.getTick() - size + 1;
    }

    public long increment() {
        return this.increment(1);
    }

    public long current() {
        final int tick = this.ticker.getTick();
        final int tickOfNewest = this.tickOfOldest + this.buckets.length - 1;
        if (tickOfNewest == tick) {
            final int indexOfNewest;
            if (this.indexOfOldest == 0) {
                indexOfNewest = this.buckets.length - 1;
            } else {
                indexOfNewest = this.indexOfOldest - 1;
            }
            return this.buckets[indexOfNewest];

        } else {
            return 0;
        }
    }

    public long increment(final long delta) {
        final int tick = this.ticker.getTick();
        final int tickOfNewest = this.tickOfOldest + this.buckets.length - 1;
        if (tickOfNewest == tick) {
            final int indexOfNewest;
            if (this.indexOfOldest == 0) {
                indexOfNewest = this.buckets.length - 1;
            } else {
                indexOfNewest = this.indexOfOldest - 1;
            }
            this.buckets[indexOfNewest] += delta;
            return this.buckets[indexOfNewest];
        }
        final int numToExpire = tick - tickOfNewest;
        if (numToExpire >= this.buckets.length) {
            //  zero everything out and start over
            this.tickOfOldest = tick - this.buckets.length + 1;
            this.buckets[0] = delta;
            this.indexOfOldest = 1;
            Arrays.fill(this.buckets, this.indexOfOldest, this.buckets.length, 0);
            return delta;
        }

        for (int i = 0; i < numToExpire - 1; i++) { //  overwrite the gap with zeros
            //  TODO: Arrays.fill(...)
            this.buckets[this.indexOfOldest++] = 0;
            if (this.indexOfOldest == this.buckets.length) {
                this.indexOfOldest = 0;
            }
        }
        this.tickOfOldest += numToExpire;

        this.buckets[this.indexOfOldest++] = delta;
        if (this.indexOfOldest == this.buckets.length) {
            this.indexOfOldest = 0;
        }

        return delta;
    }

    /**
     * Create a snapshot of the data from newest (at position 0) to the oldest (at end) data stored
     * in this recent events counter.
     *
     * @return An array of longs containing a copy of data in the window.
     */
    public long[] snapshot() {
        this.increment(0); //  refresh just in case it's been idle a long time
        final long[] range = new long[this.buckets.length];
        if (this.indexOfOldest != 0) {
            final int lengthOfFirstSection = this.buckets.length - this.indexOfOldest;
            System.arraycopy(this.buckets, this.indexOfOldest, range, 0, lengthOfFirstSection);
            System.arraycopy(
                    this.buckets,
                    0,
                    range,
                    lengthOfFirstSection,
                    this.buckets.length - lengthOfFirstSection); //  wrap around
        } else {
            System.arraycopy(this.buckets, this.indexOfOldest, range, 0, this.buckets.length);
        }
        Collections.reverse(Longs.asList(range));
        return range;
    }

    public long sum() {
        this.increment(0); //  refresh just in case it's been idle a long time
        long sum = 0;
        for (int i = 0; i < this.buckets.length; i++) {
            sum += this.buckets[i];
        }
        return sum;
    }

    public int getLength() {
        return this.buckets.length;
    }

    public interface Ticker {
        int getTick();
    }

    /** newest at 0, oldest at end */
    @Override
    public String toString() {
        this.increment(0); //  refresh just in case it's been idle a long time
        final StringBuilder sb = new StringBuilder(250);
        if (this.indexOfOldest == 0) {
            for (int i = this.buckets.length - 1; i >= 0; i--) {
                sb.append(this.buckets[i]);
                if (i != 0) {
                    sb.append(',');
                }
            }
        } else {
            for (int i = this.indexOfOldest - 1; i >= 0; i--) {
                sb.append(this.buckets[i]);
                sb.append(',');
            }
            for (int i = this.buckets.length - 1; i >= this.indexOfOldest; i--) {
                sb.append(this.buckets[i]);
                if (i != this.indexOfOldest) {
                    sb.append(',');
                }
            }
        }

        return sb.toString();
    }

    public static final Ticker SECOND_TICKER =
            new Ticker() {
                @Override
                public int getTick() {
                    return (int) (System.currentTimeMillis() / 1000);
                }
            };

    public static final Ticker MINUTE_TICKER =
            new Ticker() {
                @Override
                public int getTick() {
                    return (int) (System.currentTimeMillis() / (60 * 1000));
                }
            };

    public static final Ticker FIFTEEN_MINUTE_TICKER =
            new Ticker() {
                @Override
                public int getTick() {
                    return (int) (System.currentTimeMillis() / (15 * 60 * 1000));
                }
            };

    /**
     * If you want your time intervals (buckets) to be controlled by some external code, use this
     * {@link Ticker} and just call {@link #tick()} to advance recording to a new interval.
     */
    public static class ManualTicker implements Ticker {
        private volatile int tick = 0;

        @Override
        public int getTick() {
            return tick;
        }

        /**
         * REQUIRES EXTERNAL SYNCHRONIZATION.
         *
         * <p>This advances the ticker to the next interval/bucket/tick.
         */
        public void tick() {
            tick++;
        }
    }
}
