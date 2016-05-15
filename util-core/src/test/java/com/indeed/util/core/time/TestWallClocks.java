package com.indeed.util.core.time;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 *
 */
public class TestWallClocks {
    @Test
    public void testWallClock() {
        final WallClock wallClock = new DefaultWallClock();

        final long now = System.currentTimeMillis();
        final long wrappedNow = wallClock.currentTimeMillis();

        Assert.assertTrue(
                "Expected sequential calls to currentTimeMillis to be equal, and these were more than 5ms distant.",
                Math.abs(wrappedNow - now) < 5);
    }

    @Test
    public void testStoppedClock() throws InterruptedException {
        final long now = System.currentTimeMillis();
        final StoppedClock wallClock = new StoppedClock();

        Thread.sleep(10);

        wallClock.set(now);
        Assert.assertEquals(
                "Expected the wall clock time to be insensitive to system time changes",
                now, wallClock.currentTimeMillis());

        wallClock.plus(10, TimeUnit.SECONDS);
        Assert.assertEquals(
                "Expected a predictable change in the reported milliseconds",
                now + 10 * 1000, wallClock.currentTimeMillis());

        wallClock.plus(-5, TimeUnit.SECONDS);
        Assert.assertEquals(
                "Expected negative changes to be accepted",
                now + 5 * 1000, wallClock.currentTimeMillis());
    }
}
