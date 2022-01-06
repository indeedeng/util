package com.indeed.util.mmap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteOrder;

/**
 * @author jplaisance
 */
public final class HeapBuffer implements BufferResource {
    private static final Logger log = LoggerFactory.getLogger(HeapBuffer.class);

    private final HeapMemory memory;

    public HeapBuffer(int length, ByteOrder endianness) {
        memory = new HeapMemory(length, endianness);
    }

    public HeapMemory memory() {
        return memory;
    }

    public void close() throws IOException {}
}
