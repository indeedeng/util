package com.indeed.util.core.threads;

import junit.framework.TestCase;

import java.util.BitSet;
import java.util.Random;

import com.indeed.util.core.TreeTimer;

/**
 * @author ahudson
 */
public class TestThreadSafeBitSet extends TestCase {
    private final Random rand = new Random();

    public void testFunctionality() {
        for (int size = 5000; size < 6000; size++) {
            ThreadSafeBitSet bitSet = new ThreadSafeBitSet(size);
            for (int i = 0; i < size; i+=3) {
                bitSet.set(i, true);
            }
            for (int i = 0; i < size; i++) {
                assertTrue(bitSet.get(i) == (i%3 == 0));
            }
            for (int i = 3; i < size; i += 6) {
                bitSet.set(i, false);
            }
            for (int i = 0; i < size; i++) {
                assertTrue(bitSet.get(i) == (i%6 == 0));
            }
        }
    }

    public void testExpand() {
        ThreadSafeBitSet bs = new ThreadSafeBitSet(100);
        for (int i = 0; i < bs.size(); i++) {
            if (i%7 == 0) bs.set(i);
        }
        ThreadSafeBitSet bs2 = ThreadSafeBitSet.expand(bs, 200);
        for (int i = 0; i < bs2.size(); i++) {
            assertEquals(i < 100 && i % 7 == 0, bs2.get(i));
        }
    }


    public void testCopyFrom() {
        ThreadSafeBitSet bs = new ThreadSafeBitSet(100);
        for (int i = 0; i < bs.size(); i++) {
            if (i%3 == 0) bs.set(i);
        }
        ThreadSafeBitSet bs2 = new ThreadSafeBitSet(200);
        for (int i = 0; i < bs2.size(); i++) {
            if (i%2 == 0) bs2.set(i);
        }
        bs2.copyFrom(bs);
        for (int i = 0; i < 100; i++) {
            assertEquals("Failed for " + i, i % 3 == 0, bs2.get(i));
        }
        for (int i = 100; i < 200; i++) {
            assertEquals("Failed for " + i, i % 2 == 0, bs2.get(i));
        }
    }

    public void testClearAll() {
        ThreadSafeBitSet bs = new ThreadSafeBitSet(100);
        for (int i = 0; i < bs.size(); i++) {
            if (i%7 == 0) bs.set(i);
        }
        bs.clearAll();
        for (int i = 0; i < bs.size(); i++) {
            assertEquals(false, bs.get(i));
        }
    }

    public void testSetAll() {
        ThreadSafeBitSet bs = new ThreadSafeBitSet(100);
        for (int i = 0; i < bs.size(); i++) {
            if (i%7 == 0) bs.set(i);
        }
        bs.setAll();
        for (int i = 0; i < bs.size(); i++) {
            assertEquals(true, bs.get(i));
        }
    }

    public void testAnd() {
        ThreadSafeBitSet a = new ThreadSafeBitSet(200);
        for (int i = 0; i < 100; i++) a.set(i);
        ThreadSafeBitSet b = new ThreadSafeBitSet(200);
        for (int i = 50; i < 150; i++) b.set(i);
        b.and(a);
        for (int i = 0; i < 200; i++) assertEquals(""+i, b.get(i), (i >= 50 && i < 100));

        for (int i = 0; i < 100; i+=2) a.clear(i);
        b.clearAll();
        for (int i = 0; i < 100; i+=3) b.set(i);
        b.and(a);
        for (int i = 0; i < 200; i++) assertEquals(""+i, b.get(i), ((i < 100) && (i%2 != 0) && (i%3 == 0)));
    }

    public void testOr() {
        ThreadSafeBitSet a = new ThreadSafeBitSet(200);
        for (int i = 0; i < 100; i++) a.set(i);
        ThreadSafeBitSet b = new ThreadSafeBitSet(200);
        b.or(a);
        for (int i = 0; i < 200; i++) assertEquals(""+i, b.get(i), i < 100);

        for (int i = 1; i < 100; i+=2) {a.clear(i);b.set(i);}
        b.or(a);
        for (int i = 0; i < 200; i++) assertEquals(""+i, b.get(i), i < 100);
    }


    public void testCardinality() {
        for (int i=0; i<32; i++) {
            final ThreadSafeBitSet smallBitSet = new ThreadSafeBitSet(i);
            smallBitSet.setAll();
            final int cardinality = smallBitSet.cardinality();
            assertEquals(smallBitSet.cardinality(), cardinality);
        }
    }

    public void testInverse() {
        final Random rand = new Random(42);
        final int size = 32 * 10 + 5;
        final ThreadSafeBitSet randomSet = new ThreadSafeBitSet(size);
        for (int i=0; i < size; i++) {
            if (rand.nextDouble() < 0.2) {
                randomSet.set(i);
            }
        }
        final int cardinality = randomSet.cardinality();
        final ThreadSafeBitSet invRandomSet = randomSet.copy();
        invRandomSet.invertAll();
        for (int i = 0; i < size; i++) {
            assertEquals(!invRandomSet.get(i), randomSet.get(i));
        }
        final int invCardinality = invRandomSet.cardinality();
        assertEquals(invCardinality, size - cardinality);
        invRandomSet.xor(randomSet);
        assertEquals(invRandomSet.cardinality(), size);
    }



    public void testXor() {
        ThreadSafeBitSet a = new ThreadSafeBitSet(200);
        for (int i = 0; i < 100; i++) a.set(i);
        ThreadSafeBitSet b = new ThreadSafeBitSet(200);
        for (int i = 50; i < 150; i++) b.set(i);
        b.and(a);
        for (int i = 0; i < 200; i++) assertEquals(""+i, b.get(i), (i >= 50 && i < 100));

        for (int i = 0; i < 100; i+=2) a.clear(i);
        b.clearAll();
        for (int i = 0; i < 100; i+=3) b.set(i);
        b.xor(a);
        for (int i = 0; i < 100; i++) assertEquals(""+i, b.get(i), ((i%2 != 0) ^ (i%3 == 0)));
        for (int i = 100; i < 200; i++) assertEquals(""+i, b.get(i), (i < 100));
    }

    public void testEquals() {
        Random rand = new Random();
        for (int size = 5000; size < 5100; size++) {
            ThreadSafeBitSet bitSet1 = new ThreadSafeBitSet(size);
            ThreadSafeBitSet bitSet2 = new ThreadSafeBitSet(size);
            for (int i = 0; i < size; i++) {
                boolean randBit = rand.nextBoolean();
                bitSet1.set(i, randBit);
                bitSet2.set(i, randBit);
            }
            assertTrue(ThreadSafeBitSet.equals(bitSet1, bitSet2));
            assertTrue(ThreadSafeBitSet.equals(null, null));
            assertFalse(ThreadSafeBitSet.equals(bitSet1, null));
            assertFalse(ThreadSafeBitSet.equals(null, bitSet1));

            // change one bit to make them unequal
            int randPos = rand.nextInt(size);
            bitSet1.set(randPos, !bitSet1.get(randPos));
            assertFalse(ThreadSafeBitSet.equals(bitSet1, bitSet2));
        }
    }

    public void perfTestSpeed() {
        int size = 100000000;
        
        for (int c = 0; c < 10; c++) {
            TreeTimer timer = new TreeTimer();
            timer.push("new implementation");
            timer.push("allocation");
            ThreadSafeBitSet tsBitSet = new ThreadSafeBitSet(size);
            timer.pop();
            timer.push("initialization");
            for (int i = 0; i < size; i+=3) {
                tsBitSet.set(i, true);
            }
            timer.pop();
            timer.push("iteration");
            int count = 0;
            for (int i = 0; i < size; i++) {
                if (tsBitSet.get(i)) count++;
            }
            timer.pop();
            timer.pop();
            tsBitSet = null;
            
            assertTrue(count == (size+2)/3);
            
            timer.push("old implementation");
            timer.push("allocation");
            BitSet oBitSet = new BitSet(size);
            timer.pop();
            timer.push("initialization");
            for (int i = 0; i < size; i+=3) {
                oBitSet.set(i, true);
            }
            timer.pop();
            timer.push("iteration");
            count = 0;
            for (int i = 0; i < size; i++) {
                if (oBitSet.get(i)) count++;
            }
            timer.pop();
            timer.pop();
    
            assertTrue(count == (size+2)/3);
            
            System.out.println(timer);
        }
    }

    public void testMaxSize() {
        ThreadSafeBitSet bitSet = new ThreadSafeBitSet(Integer.MAX_VALUE);
        bitSet.set(Integer.MAX_VALUE-1, true);
        assertTrue(bitSet.get(Integer.MAX_VALUE-1));
    }

    // ok this isn't a guaranteed good test but it is A test at least
    public void slowTestThreadSafety() {
        int num = 20;
        ThreadSafeBitSet bitSet = new ThreadSafeBitSet(2000);
        ReaderThread threads [] = new ReaderThread[num];
        for (int i = 0; i < num; i++) {
            threads[i] = new ReaderThread(bitSet, i*100);
            (new Thread(threads[i])).start();
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < bitSet.size(); i+= 3) {
            bitSet.set(i, true);
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < num; i++) {
            threads[i].stop();
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int count = 0;
        for (int i = 0; i < num; i++) {
            count += threads[i].foundIt?1:0; 
        }
        assertTrue(count == 7);
    }

    public void testCopyingOr() {
        ThreadSafeBitSet a = new ThreadSafeBitSet(100);
        for (int i = 0; i < 100; i++) a.set(i);
        ThreadSafeBitSet b = new ThreadSafeBitSet(200);
        ThreadSafeBitSet result = ThreadSafeBitSet.or(a, b);
        assertEquals(200, result.size());
        for (int i = 0; i < 200; i++) assertEquals(result.get(i), i < 100);

        for (int i = 1; i < 100; i+=2) {a.clear(i);b.set(i);}
        result = ThreadSafeBitSet.or(a, b);
        assertEquals(200, result.size());
        for (int i = 0; i < 200; i++) assertEquals(""+i, result.get(i), i < 100);
    }

    public void testCopyFromRange() {
        // start at 0 in both
        copyFromRangeCase(0, 0, 3);
        copyFromRangeCase(0, 0, 20);
        copyFromRangeCase(0, 0, 31);
        copyFromRangeCase(0, 0, 32);
        copyFromRangeCase(0, 0, 33);
        copyFromRangeCase(0, 0, 63);
        copyFromRangeCase(0, 0, 64);
        copyFromRangeCase(0, 0, 65);

        // not starting at 0 but int-aligned
        copyFromRangeCase(1, 65, 7);
        copyFromRangeCase(1, 65, 8);
        copyFromRangeCase(1, 65, 9);
        copyFromRangeCase(1, 65, 15);
        copyFromRangeCase(1, 65, 16);
        copyFromRangeCase(1, 65, 17);
        copyFromRangeCase(1, 65, 31);
        copyFromRangeCase(1, 65, 32);
        copyFromRangeCase(1, 65, 33);
        copyFromRangeCase(1, 65, 63);
        copyFromRangeCase(1, 65, 64);
        copyFromRangeCase(1, 65, 65);
        copyFromRangeCase(1, 65, 80);

        // not starting at 0, not aligned
        for (int i = 1; i < 100; ++i) {
            copyFromRangeCase(33, 95, i);
            copyFromRangeCase(95, 33, i);

            copyFromRangeCase(32, 95, i);
            copyFromRangeCase(95, 32, i);

            copyFromRangeCase(33, 96, i);
            copyFromRangeCase(96, 33, i);
        }
    }

    private void copyFromRangeCase(final int start, final int otherStart, final int length) {
        final ThreadSafeBitSet dest = new ThreadSafeBitSet(start + length + 200);
        final ThreadSafeBitSet source = new ThreadSafeBitSet(otherStart + length + 200);

        for (int i = 0; i < 20; ++i) {
            final ThreadSafeBitSet expected = new ThreadSafeBitSet(dest.size());
            dest.clearAll();
            source.clearAll();

            for (int index = 0; index < start; ++index) {
                final boolean b = rand.nextBoolean();
                expected.set(index, b);
                dest.set(index, b);
            }

            for (int index = start; index < start + length; ++index) {
                dest.set(index, rand.nextBoolean());
            }

            for (int index = start + length; index < dest.size(); ++index) {
                final boolean b = rand.nextBoolean();
                expected.set(index, b);
                dest.set(index, b);
            }

            for (int index = 0; index < otherStart; ++index) {
                source.set(index, rand.nextBoolean());
            }

            for (int index = otherStart; index < otherStart + length; ++index) {
                final boolean b = rand.nextBoolean();
                expected.set(index - otherStart + start, b);
                source.set(index, b);
            }

            for (int index = otherStart + length; index < source.size(); ++index) {
                source.set(index, rand.nextBoolean());
            }

            dest.copyFromRange(source, start, otherStart, length);

            for (int index = 0; index < dest.size(); ++index) {
                assertEquals(
                        "mismatch at index " + index + " with start=" + start + ", otherStart=" + otherStart + ", length=" + length,
                        expected.get(index),
                        dest.get(index)
                );
            }
        }
    }

    private static class ReaderThread implements Runnable {
        final ThreadSafeBitSet bitSet;
        final int target;
        boolean foundIt;
        boolean stop;
        
        ReaderThread(ThreadSafeBitSet bitSet, int target) { 
            this.bitSet = bitSet;
            this.target = target;
        }
        
        public void stop() {
            this.stop = true;
        }
        
        public void run() {
            while (!foundIt && !stop) {
                foundIt = bitSet.get(target);
            }
        }
    }
}