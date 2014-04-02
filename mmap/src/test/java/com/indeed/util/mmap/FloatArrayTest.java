package com.indeed.util.mmap;

import junit.framework.TestCase;

import java.io.File;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

/**
 * @author goodwin
 */
public class FloatArrayTest extends TestCase {
    int length = 10240;
    int size = 16;   // bytes
    int maxValue = (int) Math.pow(2, size*8);

    FloatArray[] floatArrays;

    public void setUp() throws Exception {
        File file = File.createTempFile("tmp", "", new File("."));
        // noinspection ResultOfMethodCallIgnored
        file.deleteOnExit();

        floatArrays = new FloatArray[3];
        floatArrays[0] = new HeapMemory(length * size, ByteOrder.LITTLE_ENDIAN).floatArray(0, length);
        floatArrays[1] = new MMapBuffer(file, 0, length * size, FileChannel.MapMode.READ_WRITE, ByteOrder.LITTLE_ENDIAN).memory().floatArray(0L, length);
        floatArrays[2] = new NativeBuffer(length * size, ByteOrder.LITTLE_ENDIAN).memory().floatArray(0L, length);
    }

    public void testFloatArray() throws Exception {
        for (FloatArray floatArray : floatArrays) {
            assertEquals(length, floatArray.length());
            floatArray.set(0, (float) 0);
            floatArray.set(1, (float) 1);
            floatArray.set(2, (float) 2);
            assertEquals(0.0f, floatArray.get(0));
            assertEquals(1.0f, floatArray.get(1));
            assertEquals(2.0f, floatArray.get(2));
            assertEquals(length, floatArray.length());
            floatArray.set(3, new float[]{3, 4});
            assertEquals(3.0f, floatArray.get(3));
            floatArray.set(5, new float[]{0, 1, 2, 3, 4, 5, 6}, 5, 2);

            for (int i = 7; i < length; i++) {
                floatArray.set(i, (float) i);
            }

            for (int i = 0; i < length; i++) {
                float value = i % maxValue;
                if (value >= maxValue/2) {
                    value = value - maxValue;
                }
                assertEquals(value, floatArray.get(i));
            }

            for (int i = 0; i < length - 8; i++) {
                float[] bytes = new float[8];
                floatArray.get(i, bytes);
                for (int j = 0; j < 8; j++) {
                    float value = (i + j) % maxValue;
                    if (value >= maxValue/2) {
                        value = value - maxValue;
                    }
                    assertEquals(value, bytes[j]);
                }
            }

            FloatArray second = floatArray.slice(2, length-2);
            assertEquals(2.0f, second.get(0));
        }
    }

    public void testThrownExceptions() {
        for (FloatArray floatArray : floatArrays) {
            try {
                floatArray.set(-1, (float) 1);
                fail();
            } catch (IndexOutOfBoundsException success) {}
            try {
                floatArray.set(-1, new float[4], 0, 2);
                fail();
            } catch (IndexOutOfBoundsException success) {}
            try {
                floatArray.set(-1, new float[2]);
                fail();
            } catch (IndexOutOfBoundsException success) {}
            try {
                floatArray.get(-1);
                fail();
            } catch (IndexOutOfBoundsException success) {}
            try {
                floatArray.get(-1, new float[4], 0, 2);
                fail();
            } catch (IndexOutOfBoundsException success) {}
            try {
                floatArray.get(-1, new float[4], 0, 2);
                fail();
            } catch (IndexOutOfBoundsException success) {}
            try {
                floatArray.slice(-1, length);
                fail();
            } catch (IndexOutOfBoundsException success) {}
        }
    }
}
