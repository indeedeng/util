package com.indeed.util.mmap;

import junit.framework.TestCase;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

/**
 * @author goodwin
 */
public class DirectMemoryTest extends TestCase {
    int length = 10000;

    DirectMemory directMemory;
    DirectMemory directMemory2;

    @Override
    public void setUp() throws Exception {
        File file = File.createTempFile("tmp", "", new File("."));
        file.deleteOnExit();

        directMemory = new MMapBuffer(file, 0L, length, FileChannel.MapMode.READ_WRITE, ByteOrder.LITTLE_ENDIAN).memory();
        directMemory2 = new MMapBuffer(file, 0L, length, FileChannel.MapMode.READ_WRITE, ByteOrder.BIG_ENDIAN).memory();
    }

    public void testByte() throws Exception {
        for (int i = 0; i < length; i++) {
            directMemory.putByte(i, (byte) i);
            directMemory2.putByte(i, (byte) i);
        }
        for (int i = 0; i < length; i++) {
            int value = i % 256;
            if (value >= 128) {
                value -= 256;
            }
            assertEquals(value, directMemory.getByte(i));
            assertEquals(value, directMemory2.getByte(i));
        }
    }

    public void testBytes() throws Exception {
        for (int i = 0; i < length-8; i++) {
            byte[] bytes = new byte[8];
            for (int j = 0; j < 8; j++) {
                bytes[j] = (byte) (i+j);
            }
            directMemory.putBytes(i, bytes, 2, 6);
            directMemory2.putBytes(i, bytes, 2, 6);
            for (int j = 0; j < 6; j++) {
                assertEquals((byte) (i+j+2), directMemory.getByte(i + j));
                assertEquals((byte) (i+j+2), directMemory2.getByte(i + j));
            }
        }
    }

    public void testDirectMemory() throws Exception {
        for (int i = 0; i < length; i+= 8) {
            File file = File.createTempFile("tmp", "", new File("."));
            DirectMemory localDirectMemory = new MMapBuffer(file, 0L, 8, FileChannel.MapMode.READ_WRITE, ByteOrder.LITTLE_ENDIAN).memory();
            for (int j = 0; j < 8; j++) {
                localDirectMemory.putByte(j, (byte) (i+j));
            }
            directMemory.putBytes(i, localDirectMemory);
            directMemory2.putBytes(i, localDirectMemory);
            file.delete();
        }

        for (int i = 0; i < directMemory.length(); i++) {
            assertEquals((byte) i, directMemory.getByte(i));
        }

        for (int i = 0; i < directMemory2.length(); i++) {
            assertEquals((byte) i, directMemory2.getByte(i));
        }
    }

    public void testHeapMemory() throws Exception {
        for (int i = 0; i < length; i+=8) {
            HeapMemory heapMemory = new HeapMemory(8, ByteOrder.LITTLE_ENDIAN);
            for (int j = 0; j < 8; j++) {
                heapMemory.putByte(j, (byte) (i+j));
            }
            directMemory.putBytes(i, heapMemory);
            directMemory2.putBytes(i, heapMemory);
        }

        for (int i = 0; i < directMemory.length(); i++) {
            assertEquals((byte) i, directMemory.getByte(i));
            assertEquals((byte) i, directMemory2.getByte(i));
        }
    }

    public void testByteBuffer() throws Exception {
        for (int i = 0; i < length; i+=8) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(8);
            for (int j = 0; j < 8; j++) {
                byteBuffer.put(j, (byte) (i+j));
            }
            directMemory.putBytes(i, byteBuffer);
            directMemory2.putBytes(i, byteBuffer);
        }

        for (int i = 0; i < directMemory.length(); i++) {
            assertEquals((byte) i, directMemory.getByte(i));
            assertEquals((byte) i, directMemory2.getByte(i));
        }
    }
}
