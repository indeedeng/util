package com.indeed.util.mmap;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.ByteOrder;

/**
 * @author jplaisance
 */
public final class HeapBuffer implements BufferResource {
    private static final Logger log = Logger.getLogger(HeapBuffer.class);

    private final HeapMemory memory;

    public HeapBuffer(int length, ByteOrder endianness) {
        memory = new HeapMemory(length, endianness);
    }

    public HeapMemory memory() {
        return memory;
    }

    public void close() throws IOException {}
}
