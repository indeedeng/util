package com.indeed.util.mmap;

import junit.framework.TestCase;

import java.nio.ByteOrder;

/**
 * @author goodwin
 */
public class HeapMemoryTest extends TestCase {
    int length = 10;

    public void testException() throws Exception {
        byte[] bytes = new byte[11];
        HeapMemory src = new HeapMemory(bytes, 0, length, ByteOrder.LITTLE_ENDIAN);
        HeapMemory dst = new HeapMemory(bytes, 0, length, ByteOrder.BIG_ENDIAN);
        try {
            src.putBytes(0, bytes);
            fail();
        } catch (IndexOutOfBoundsException success) {}
        src.getBytes(0, dst);
    }
}
