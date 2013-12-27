package com.indeed.util.core;

import com.indeed.util.core.LongRecentEventsCounter.Ticker;
import junit.framework.TestCase;

/**
 * @author ketan
 *
 */
public class LongRecentEventsCounterTest extends TestCase {
    class TestTicker implements Ticker {
        private int tick = 0;

        @Override
        public int getTick() {
            return tick;
        }

        public void setTick(final int tick) {
            this.tick = tick;
        }
    }

    public void test2() {
        final TestTicker testTicker = new TestTicker();
        final LongRecentEventsCounter counter = new LongRecentEventsCounter(testTicker, 2);
        counter.increment(4);
        {
            assertEquals(4, counter.sum());
            final long[] snapshot = counter.snapshot();
            assertEquals(4, snapshot[0]);
            assertEquals(0, snapshot[1]);
            assertEquals("4,0", counter.toString());
            assertEquals(4, counter.current());
        }
        testTicker.setTick(1);
        counter.increment(1);
        {
            assertEquals(5, counter.sum());
            final long[] snapshot = counter.snapshot();
            assertEquals(1, snapshot[0]);
            assertEquals(4, snapshot[1]);
            assertEquals("1,4", counter.toString());
            assertEquals(1, counter.current());
        }
        testTicker.setTick(2);
        counter.increment(7);
        {
            assertEquals(8, counter.sum());
            final long[] snapshot = counter.snapshot();
            assertEquals(7, snapshot[0]);
            assertEquals(1, snapshot[1]);
            assertEquals("7,1", counter.toString());
            assertEquals(7, counter.current());
        }
        testTicker.setTick(6);
        counter.increment(11);
        {
            assertEquals(11, counter.sum());
            final long[] snapshot = counter.snapshot();
            assertEquals(11, snapshot[0]);
            assertEquals(0, snapshot[1]);
            assertEquals("11,0", counter.toString());
            assertEquals(11, counter.current());
        }
        testTicker.setTick(7);
        // no increment
        {
            assertEquals("0,11", counter.toString());
            assertEquals(0, counter.current());
        }
    }

    public void test4() {
        final TestTicker testTicker = new TestTicker();
        final LongRecentEventsCounter counter = new LongRecentEventsCounter(testTicker, 4);
        counter.increment(4); //  { 4 }
        {
            assertEquals(4, counter.sum());
            final long[] snapshot = counter.snapshot();
            assertEquals(4, snapshot[0]);
            assertEquals(0, snapshot[1]);
            assertEquals(0, snapshot[2]);
            assertEquals(0, snapshot[3]);
            assertEquals("4,0,0,0", counter.toString());
            assertEquals(4, counter.current());
        }
        testTicker.setTick(1);
        counter.increment(1); //  { 4, 1 }
        {
            assertEquals(5, counter.sum());
            final long[] snapshot = counter.snapshot();
            assertEquals(1, snapshot[0]);
            assertEquals(4, snapshot[1]);
            assertEquals(0, snapshot[2]);
            assertEquals(0, snapshot[3]);
            assertEquals("1,4,0,0", counter.toString());
            assertEquals(1, counter.current());
        }
        testTicker.setTick(2);
        counter.increment(7); //  { 4, 1, 7 }}
        {
            assertEquals(12, counter.sum());
            final long[] snapshot = counter.snapshot();
            assertEquals(7, snapshot[0]);
            assertEquals(1, snapshot[1]);
            assertEquals(4, snapshot[2]);
            assertEquals(0, snapshot[3]);
            assertEquals("7,1,4,0", counter.toString());
            assertEquals(7, counter.current());
        }
        testTicker.setTick(3);
        counter.increment(13); //  { 4, 1, 7, 13 }}
        {
            assertEquals(25, counter.sum());
            final long[] snapshot = counter.snapshot();
            assertEquals(13, snapshot[0]);
            assertEquals(7, snapshot[1]);
            assertEquals(1, snapshot[2]);
            assertEquals(4, snapshot[3]);
            assertEquals("13,7,1,4", counter.toString());
            assertEquals(13, counter.current());
        }
        testTicker.setTick(4);
        counter.increment(5); //  { 1, 7, 13, 5 }}
        {
            assertEquals(26, counter.sum());
            final long[] snapshot = counter.snapshot();
            assertEquals(5, snapshot[0]);
            assertEquals(13, snapshot[1]);
            assertEquals(7, snapshot[2]);
            assertEquals(1, snapshot[3]);
            assertEquals("5,13,7,1", counter.toString());
            assertEquals(5, counter.current());
        }

        testTicker.setTick(6);
        counter.increment(11);    //  { 13, 5, 0, 11 }
        {
            assertEquals(29, counter.sum());
            final long[] snapshot = counter.snapshot();
            assertEquals(11, snapshot[0]);
            assertEquals(0, snapshot[1]);
            assertEquals(5, snapshot[2]);
            assertEquals(13, snapshot[3]);
            assertEquals("11,0,5,13", counter.toString());
            assertEquals(11, counter.current());
        }

        testTicker.setTick(7);
        counter.increment(4);    //  { 5, 0, 11, 4 }
        {
            assertEquals(20, counter.sum());
            final long[] snapshot = counter.snapshot();
            assertEquals(4, snapshot[0]);
            assertEquals(11, snapshot[1]);
            assertEquals(0, snapshot[2]);
            assertEquals(5, snapshot[3]);
            assertEquals("4,11,0,5", counter.toString());
            assertEquals(4, counter.current());
        }

        testTicker.setTick(10);
        counter.increment(2);    //  { 4, 0, 0, 2}
        {
            assertEquals(6, counter.sum());
            final long[] snapshot = counter.snapshot();
            assertEquals(2, snapshot[0]);
            assertEquals(0, snapshot[1]);
            assertEquals(0, snapshot[2]);
            assertEquals(4, snapshot[3]);
            assertEquals("2,0,0,4", counter.toString());
            assertEquals(2, counter.current());
        }

        testTicker.setTick(15);
        counter.increment(99);    //  { 0, 0, 0, 99}
        {
            assertEquals(99, counter.sum());
            final long[] snapshot = counter.snapshot();
            assertEquals(99, snapshot[0]);
            assertEquals(0, snapshot[1]);
            assertEquals(0, snapshot[2]);
            assertEquals(0, snapshot[3]);
            assertEquals("99,0,0,0", counter.toString());
            assertEquals(99, counter.current());
        }

        //  NO tick

        counter.increment(14);    //  { 0, 0, 0, 99}
        {
            assertEquals(113, counter.sum());
            final long[] snapshot = counter.snapshot();
            assertEquals(113, snapshot[0]);
            assertEquals(0, snapshot[1]);
            assertEquals(0, snapshot[2]);
            assertEquals(0, snapshot[3]);
            assertEquals("113,0,0,0", counter.toString());
            assertEquals(113, counter.current());
        }
        testTicker.setTick(testTicker.getTick() + 1);
        // no increment
        {
            assertEquals("0,113,0,0", counter.toString());
            assertEquals(0, counter.current());
        }
    }
}
