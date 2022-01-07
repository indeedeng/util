package com.indeed.util.mmap;

import junit.framework.TestCase;

import java.io.File;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

/** @author goodwin */
public class LongArrayTest extends TestCase {
    int length = 10000;
    int size = 8;
    int maxValue = (int) Math.pow(2, 8 * size);

    LongArray[] longArrays;

    public void setUp() throws Exception {
        File file = File.createTempFile("tmp", "", new File("."));
        // noinspection ResultOfMethodCallIgnored
        file.deleteOnExit();

        longArrays = new LongArray[3];
        longArrays[0] = new HeapMemory(length * size, ByteOrder.LITTLE_ENDIAN).longArray(0, length);
        longArrays[1] =
                new MMapBuffer(
                                file,
                                0,
                                length * size,
                                FileChannel.MapMode.READ_WRITE,
                                ByteOrder.LITTLE_ENDIAN)
                        .memory()
                        .longArray(0L, length);
        longArrays[2] =
                new NativeBuffer(length * size, ByteOrder.LITTLE_ENDIAN)
                        .memory()
                        .longArray(0L, length);
    }

    public void testLongArray() throws Exception {
        for (LongArray longArray : longArrays) {
            assertEquals(length, longArray.length());
            longArray.set(0, 0);
            longArray.set(1, 1);
            longArray.set(2, 2);
            assertEquals(0, longArray.get(0));
            assertEquals(1, longArray.get(1));
            assertEquals(2, longArray.get(2));
            assertEquals(length, longArray.length());
            longArray.set(3, new long[] {3, 4});
            assertEquals(3, longArray.get(3));
            longArray.set(5, new long[] {0, 1, 2, 3, 4, 5, 6}, 5, 2);

            for (int i = 7; i < length; i++) {
                longArray.set(i, i);
            }

            for (int i = 0; i < length; i++) {
                int value = i % maxValue;
                if (value >= maxValue / 2) {
                    value = value - maxValue;
                }
                assertEquals(value, longArray.get(i));
            }

            for (int i = 0; i < length - 8; i++) {
                long[] ints = new long[8];
                longArray.get(i, ints);
                for (int j = 0; j < 8; j++) {
                    long value = (i + j) % maxValue;
                    if (value >= maxValue / 2) {
                        value = value - maxValue;
                    }
                    assertEquals(value, ints[j]);
                }
            }

            LongArray second = longArray.slice(2, length - 2);
            assertEquals(2, second.get(0));
        }
    }

    public void testThrownExceptions() {
        for (LongArray longArray : longArrays) {
            try {
                longArray.set(-1, 1);
                fail();
            } catch (IndexOutOfBoundsException success) {
            }
            try {
                longArray.set(-1, new long[4], 0, 2);
                fail();
            } catch (IndexOutOfBoundsException success) {
            }
            try {
                longArray.set(-1, new long[2]);
                fail();
            } catch (IndexOutOfBoundsException success) {
            }
            try {
                longArray.get(-1);
                fail();
            } catch (IndexOutOfBoundsException success) {
            }
            try {
                longArray.get(-1, new long[4], 0, 2);
                fail();
            } catch (IndexOutOfBoundsException success) {
            }
            try {
                longArray.get(-1, new long[4], 0, 2);
                fail();
            } catch (IndexOutOfBoundsException success) {
            }
            try {
                longArray.slice(-1, length);
                fail();
            } catch (IndexOutOfBoundsException success) {
            }
        }
    }
}
