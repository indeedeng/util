package com.indeed.util.mmap;

import junit.framework.TestCase;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.ByteOrder;

/**
 * @author goodwin
 */
public class NativeBufferTest extends TestCase {
    private static final Logger log = Logger.getLogger(NativeBufferTest.class);

    long length = 10000;

    NativeBuffer nativeBuffer;

    public void setUp() throws Exception {
        nativeBuffer = new NativeBuffer(length, ByteOrder.LITTLE_ENDIAN);
    }

    public void tearDown() throws Exception {
        nativeBuffer.close();
    }

    public void testMMapThreshold() throws IOException {
        final NativeBuffer buffer = new NativeBuffer(1024*1024*1024+1, ByteOrder.LITTLE_ENDIAN);
        buffer.memory().putByte(1024*1024*1024, (byte)3);
        final NativeBuffer buffer2 = new NativeBuffer(1024*1024*1024+1, ByteOrder.LITTLE_ENDIAN);
        assertEquals(buffer2.memory().getByte(1024*1024*1024), 0);
        buffer.close();
    }

    public void testMemory() throws Exception {
        nativeBuffer.memory();
    }
}
