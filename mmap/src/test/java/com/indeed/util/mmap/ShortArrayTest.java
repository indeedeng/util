package com.indeed.util.mmap;

import junit.framework.TestCase;

import java.io.File;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

/**
 * @author goodwin
 */
public class ShortArrayTest extends TestCase {
    int length = 10000;
    int size = 2;
    int maxValue = (int) Math.pow(2, 8*size);

    ShortArray[] shortArrays;

    public void setUp() throws Exception {
        File file = File.createTempFile("tmp", "", new File("."));
        // noinspection ResultOfMethodCallIgnored
        file.deleteOnExit();

        shortArrays = new ShortArray[3];
        shortArrays[0] = new HeapMemory(length*size, ByteOrder.LITTLE_ENDIAN).shortArray(0, length);
        shortArrays[1] = new MMapBuffer(file, 0, length*size, FileChannel.MapMode.READ_WRITE, ByteOrder.LITTLE_ENDIAN).memory().shortArray(0L, length);
        shortArrays[2] = new NativeBuffer(length*size, ByteOrder.LITTLE_ENDIAN).memory().shortArray(0L, length);
    }

    public void testShortArray() throws Exception {
        for (ShortArray shortArray : shortArrays) {
            assertEquals(length, shortArray.length());
            shortArray.set(0, (short) 0);
            shortArray.set(1, (short) 1);
            shortArray.set(2, (short) 2);
            assertEquals(0, shortArray.get(0));
            assertEquals(1, shortArray.get(1));
            assertEquals(2, shortArray.get(2));
            assertEquals(length, shortArray.length());
            shortArray.set(3, new short[]{3, 4});
            assertEquals(3, shortArray.get(3));
            shortArray.set(5, new short[]{0, 1, 2, 3, 4, 5, 6}, 5, 2);

            for (int i = 7; i < length; i++) {
                shortArray.set(i, (short) i);
            }

            for (int i = 0; i < length; i++) {
                int value = i % maxValue;
                if (value >= maxValue/2) {
                    value = value - maxValue;
                }
                assertEquals(value, shortArray.get(i));
            }

            for (int i = 0; i < length - 8; i++) {
                short[] ints = new short[8];
                shortArray.get(i, ints);
                for (int j = 0; j < 8; j++) {
                    int value = (i + j) % maxValue;
                    if (value >= maxValue/2) {
                        value = value - maxValue;
                    }
                    assertEquals(value, ints[j]);
                }
            }

            ShortArray second = shortArray.slice(2, length-2);
            assertEquals(2, second.get(0));
        }
    }

    public void testThrownExceptions() {
        for (ShortArray shortArray : shortArrays) {
            try {
                shortArray.set(-1, (short) 1);
                fail();
            } catch (IndexOutOfBoundsException success) {}
            try {
                shortArray.set(-1, new short[4], 0, 2);
                fail();
            } catch (IndexOutOfBoundsException success) {}
            try {
                shortArray.set(-1, new short[2]);
                fail();
            } catch (IndexOutOfBoundsException success) {}
            try {
                shortArray.get(-1);
                fail();
            } catch (IndexOutOfBoundsException success) {}
            try {
                shortArray.get(-1, new short[4], 0, 2);
                fail();
            } catch (IndexOutOfBoundsException success) {}
            try {
                shortArray.get(-1, new short[4], 0, 2);
                fail();
            } catch (IndexOutOfBoundsException success) {}
            try {
                shortArray.slice(-1, length);
                fail();
            } catch (IndexOutOfBoundsException success) {}
        }
    }
}
