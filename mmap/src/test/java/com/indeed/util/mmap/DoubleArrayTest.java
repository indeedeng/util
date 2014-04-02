package com.indeed.util.mmap;

import junit.framework.TestCase;

import java.io.File;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

/**
 * @author goodwin
 */
public class DoubleArrayTest extends TestCase {
    int length = 10240;
    int size = 16;   // bytes
    int maxValue = (int) Math.pow(2, size*8);

    DoubleArray[] doubleArrays;

    public void setUp() throws Exception {
        File file = File.createTempFile("tmp", "", new File("."));
        // noinspection ResultOfMethodCallIgnored
        file.deleteOnExit();

        doubleArrays = new DoubleArray[3];
        doubleArrays[0] = new HeapMemory(length * size, ByteOrder.LITTLE_ENDIAN).doubleArray(0, length);
        doubleArrays[1] = new MMapBuffer(file, 0, length * size, FileChannel.MapMode.READ_WRITE, ByteOrder.LITTLE_ENDIAN).memory().doubleArray(0L, length);
        doubleArrays[2] = new NativeBuffer(length * size, ByteOrder.LITTLE_ENDIAN).memory().doubleArray(0L, length);
    }

    public void testDoubleArray() throws Exception {
        for (DoubleArray doubleArray : doubleArrays) {
            assertEquals(length, doubleArray.length());
            doubleArray.set(0, (double) 0);
            doubleArray.set(1, (double) 1);
            doubleArray.set(2, (double) 2);
            assertEquals(0.0, doubleArray.get(0));
            assertEquals(1.0, doubleArray.get(1));
            assertEquals(2.0, doubleArray.get(2));
            assertEquals(length, doubleArray.length());
            doubleArray.set(3, new double[]{3, 4});
            assertEquals(3.0, doubleArray.get(3));
            doubleArray.set(5, new double[]{0, 1, 2, 3, 4, 5, 6}, 5, 2);

            for (int i = 7; i < length; i++) {
                doubleArray.set(i, (double) i);
            }

            for (int i = 0; i < length; i++) {
                double value = i % maxValue;
                if (value >= maxValue/2) {
                    value = value - maxValue;
                }
                assertEquals(value, doubleArray.get(i));
            }

            for (int i = 0; i < length - 8; i++) {
                double[] bytes = new double[8];
                doubleArray.get(i, bytes);
                for (int j = 0; j < 8; j++) {
                    double value = (i + j) % maxValue;
                    if (value >= maxValue/2) {
                        value = value - maxValue;
                    }
                    assertEquals(value, bytes[j]);
                }
            }

            DoubleArray second = doubleArray.slice(2, length-2);
            assertEquals(2.0, second.get(0));
        }
    }

    public void testThrownExceptions() {
        for (DoubleArray doubleArray : doubleArrays) {
            try {
                doubleArray.set(-1, (double) 1);
                fail();
            } catch (IndexOutOfBoundsException success) {}
            try {
                doubleArray.set(-1, new double[4], 0, 2);
                fail();
            } catch (IndexOutOfBoundsException success) {}
            try {
                doubleArray.set(-1, new double[2]);
                fail();
            } catch (IndexOutOfBoundsException success) {}
            try {
                doubleArray.get(-1);
                fail();
            } catch (IndexOutOfBoundsException success) {}
            try {
                doubleArray.get(-1, new double[4], 0, 2);
                fail();
            } catch (IndexOutOfBoundsException success) {}
            try {
                doubleArray.get(-1, new double[4], 0, 2);
                fail();
            } catch (IndexOutOfBoundsException success) {}
            try {
                doubleArray.slice(-1, length);
                fail();
            } catch (IndexOutOfBoundsException success) {}
        }
    }
}
