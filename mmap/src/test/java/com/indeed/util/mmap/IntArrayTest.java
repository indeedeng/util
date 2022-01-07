package com.indeed.util.mmap;

import junit.framework.TestCase;

import java.io.File;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

/** @author goodwin */
public class IntArrayTest extends TestCase {
    int length = 10000;
    int size = 4;
    int maxValue = (int) Math.pow(2, 8 * size);

    IntArray[] intArrays;

    public void setUp() throws Exception {
        File file = File.createTempFile("tmp", "", new File("."));
        // noinspection ResultOfMethodCallIgnored
        file.deleteOnExit();

        intArrays = new IntArray[3];
        intArrays[0] = new HeapMemory(length * size, ByteOrder.LITTLE_ENDIAN).intArray(0, length);
        intArrays[1] =
                new MMapBuffer(
                                file,
                                0,
                                length * size,
                                FileChannel.MapMode.READ_WRITE,
                                ByteOrder.LITTLE_ENDIAN)
                        .memory()
                        .intArray(0L, length);
        intArrays[2] =
                new NativeBuffer(length * size, ByteOrder.LITTLE_ENDIAN)
                        .memory()
                        .intArray(0L, length);
    }

    public void testIntArray() throws Exception {
        for (IntArray intArray : intArrays) {
            assertEquals(length, intArray.length());
            intArray.set(0, 0);
            intArray.set(1, 1);
            intArray.set(2, 2);
            assertEquals(0, intArray.get(0));
            assertEquals(1, intArray.get(1));
            assertEquals(2, intArray.get(2));
            assertEquals(length, intArray.length());
            intArray.set(3, new int[] {3, 4});
            assertEquals(3, intArray.get(3));
            intArray.set(5, new int[] {0, 1, 2, 3, 4, 5, 6}, 5, 2);

            for (int i = 7; i < length; i++) {
                intArray.set(i, i);
            }

            for (int i = 0; i < length; i++) {
                int value = i % maxValue;
                if (value >= maxValue / 2) {
                    value = value - maxValue;
                }
                assertEquals(value, intArray.get(i));
            }

            for (int i = 0; i < length - 8; i++) {
                int[] ints = new int[8];
                intArray.get(i, ints);
                for (int j = 0; j < 8; j++) {
                    int value = (i + j) % maxValue;
                    if (value >= maxValue / 2) {
                        value = value - maxValue;
                    }
                    assertEquals(value, ints[j]);
                }
            }

            IntArray second = intArray.slice(2, length - 2);
            assertEquals(2, second.get(0));
        }
    }

    public void testThrownExceptions() {
        for (IntArray intArray : intArrays) {
            try {
                intArray.set(-1, 1);
                fail();
            } catch (IndexOutOfBoundsException success) {
            }
            try {
                intArray.set(-1, new int[4], 0, 2);
                fail();
            } catch (IndexOutOfBoundsException success) {
            }
            try {
                intArray.set(-1, new int[2]);
                fail();
            } catch (IndexOutOfBoundsException success) {
            }
            try {
                intArray.get(-1);
                fail();
            } catch (IndexOutOfBoundsException success) {
            }
            try {
                intArray.get(-1, new int[4], 0, 2);
                fail();
            } catch (IndexOutOfBoundsException success) {
            }
            try {
                intArray.get(-1, new int[4], 0, 2);
                fail();
            } catch (IndexOutOfBoundsException success) {
            }
            try {
                intArray.slice(-1, length);
                fail();
            } catch (IndexOutOfBoundsException success) {
            }
        }
    }
}
