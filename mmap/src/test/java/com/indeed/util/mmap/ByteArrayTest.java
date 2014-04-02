package com.indeed.util.mmap;

import junit.framework.TestCase;

import java.io.File;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

/**
 * @author goodwin
 */
public class ByteArrayTest extends TestCase {

    int length = 10000;

    ByteArray[] byteArrays;

    public void setUp() throws Exception {
        File file = File.createTempFile("tmp", "", new File("."));
        // noinspection ResultOfMethodCallIgnored
        file.deleteOnExit();

        byteArrays = new ByteArray[3];
        byteArrays[0] = new HeapMemory(length, ByteOrder.LITTLE_ENDIAN).byteArray(0, length);
        byteArrays[1] = new MMapBuffer(file, 0, length, FileChannel.MapMode.READ_WRITE, ByteOrder.LITTLE_ENDIAN).memory().byteArray(0L, length);
        byteArrays[2] = new NativeBuffer(length, ByteOrder.LITTLE_ENDIAN).memory().byteArray(0L, length);
    }

    public void testByteArray() throws Exception {
        for (ByteArray byteArray : byteArrays) {
            assertEquals(length, byteArray.length());
            byteArray.set(0, (byte) 0);
            byteArray.set(1, (byte) 1);
            byteArray.set(2, (byte) 2);
            assertEquals(0, byteArray.get(0));
            assertEquals(1, byteArray.get(1));
            assertEquals(2, byteArray.get(2));
            assertEquals(length, byteArray.length());
            byteArray.set(3, new byte[]{3, 4});
            assertEquals(3, byteArray.get(3));
            byteArray.set(5, new byte[]{0, 1, 2, 3, 4, 5, 6}, 5, 2);

            for (int i = 7; i < length; i++) {
                byteArray.set(i, (byte) i);
            }

            for (int i = 0; i < length; i++) {
                int value = i % 256;
                if (value >= 128) {
                    value = value - 256;
                }
                assertEquals(value, byteArray.get(i));
            }

            for (int i = 0; i < length - 8; i++) {
                byte[] bytes = new byte[8];
                byteArray.get(i, bytes);
                for (int j = 0; j < 8; j++) {
                    int value = (i + j) % 256;
                    if (value >= 128) {
                        value = value - 256;
                    }
                    assertEquals(value, bytes[j]);
                }
            }

            ByteArray second = byteArray.slice(2, length-2);
            assertEquals(2, second.get(0));
        }
    }

    public void testThrownExceptions() {
        for (ByteArray byteArray : byteArrays) {
            try {
                byteArray.set(-1, (byte) 1);
                fail();
            } catch (IndexOutOfBoundsException success) {}
            try {
                byteArray.set(-1, new byte[4], 0, 2);
                fail();
            } catch (IndexOutOfBoundsException success) {}
            try {
                byteArray.set(-1, new byte[2]);
                fail();
            } catch (IndexOutOfBoundsException success) {}
            try {
                byteArray.get(-1);
                fail();
            } catch (IndexOutOfBoundsException success) {}
            try {
                byteArray.get(-1, new byte[4], 0, 2);
                fail();
            } catch (IndexOutOfBoundsException success) {}
            try {
                byteArray.get(-1, new byte[4], 0, 2);
                fail();
            } catch (IndexOutOfBoundsException success) {}
            try {
                byteArray.slice(-1, length);
                fail();
            } catch (IndexOutOfBoundsException success) {}
        }
    }
}
