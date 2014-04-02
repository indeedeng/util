package com.indeed.util.mmap;

import junit.framework.TestCase;

import java.io.File;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

/**
 * @author goodwin
 */
public class CharArrayTest extends TestCase {
    int length = 10240;
    int size = 4;   // bytes
    int maxValue = (int) Math.pow(2, size*8);

    CharArray[] charArrays;

    public void setUp() throws Exception {
        File file = File.createTempFile("tmp", "", new File("."));
        // noinspection ResultOfMethodCallIgnored
        file.deleteOnExit();

        charArrays = new CharArray[3];
        charArrays[0] = new HeapMemory(length * size, ByteOrder.LITTLE_ENDIAN).charArray(0, length);
        charArrays[1] = new MMapBuffer(file, 0, length * size, FileChannel.MapMode.READ_WRITE, ByteOrder.LITTLE_ENDIAN).memory().charArray(0L, length);
        charArrays[2] = new NativeBuffer(length * size, ByteOrder.LITTLE_ENDIAN).memory().charArray(0L, length);
    }

    public void testByteArray() throws Exception {
        for (CharArray charArray : charArrays) {
            assertEquals(length, charArray.length());
            charArray.set(0, (char) 0);
            charArray.set(1, (char) 1);
            charArray.set(2, (char) 2);
            assertEquals(0, charArray.get(0));
            assertEquals(1, charArray.get(1));
            assertEquals(2, charArray.get(2));
            assertEquals(length, charArray.length());
            charArray.set(3, new char[]{3, 4});
            assertEquals(3, charArray.get(3));
            charArray.set(5, new char[]{0, 1, 2, 3, 4, 5, 6}, 5, 2);

            for (int i = 7; i < length; i++) {
                charArray.set(i, (char) i);
            }

            for (int i = 0; i < length; i++) {
                int value = i % maxValue;
                if (value >= maxValue/2) {
                    value = value - maxValue;
                }
                assertEquals(value, charArray.get(i));
            }

            for (int i = 0; i < length - 8; i++) {
                char[] bytes = new char[8];
                charArray.get(i, bytes);
                for (int j = 0; j < 8; j++) {
                    int value = (i + j) % maxValue;
                    if (value >= maxValue/2) {
                        value = value - maxValue;
                    }
                    assertEquals(value, bytes[j]);
                }
            }

            CharArray second = charArray.slice(2, length-2);
            assertEquals(2, second.get(0));
        }
    }

    public void testThrownExceptions() {
        for (CharArray charArray : charArrays) {
            try {
                charArray.set(-1, (char) 1);
                fail();
            } catch (IndexOutOfBoundsException success) {}
            try {
                charArray.set(-1, new char[4], 0, 2);
                fail();
            } catch (IndexOutOfBoundsException success) {}
            try {
                charArray.set(-1, new char[2]);
                fail();
            } catch (IndexOutOfBoundsException success) {}
            try {
                charArray.get(-1);
                fail();
            } catch (IndexOutOfBoundsException success) {}
            try {
                charArray.get(-1, new char[4], 0, 2);
                fail();
            } catch (IndexOutOfBoundsException success) {}
            try {
                charArray.get(-1, new char[4], 0, 2);
                fail();
            } catch (IndexOutOfBoundsException success) {}
            try {
                charArray.slice(-1, length);
                fail();
            } catch (IndexOutOfBoundsException success) {}
        }
    }
}
