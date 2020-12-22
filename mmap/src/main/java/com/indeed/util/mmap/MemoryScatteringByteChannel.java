package com.indeed.util.mmap;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ScatteringByteChannel;

/**
* @author jplaisance
*/
public final class MemoryScatteringByteChannel implements ScatteringByteChannel {

    private static final Logger log = LogManager.getLogger(MemoryScatteringByteChannel.class);

    private boolean closed = false;

    private final Memory memory;
    private long position = 0;

    public MemoryScatteringByteChannel(Memory memory) {
        this.memory = memory;
    }

    @Override
    public long read(ByteBuffer[] dsts, final int offset, final int length) throws IOException {
        if (offset < 0) throw new IndexOutOfBoundsException(String.valueOf(offset));
        if (length < 0) throw new IndexOutOfBoundsException(String.valueOf(length));
        if (offset+length > dsts.length) throw new IndexOutOfBoundsException(String.valueOf(offset+length));
        long total = 0;
        for (int i = offset; i < offset+length; i++) {
            final int read = read(dsts[i]);
            if (read < 0) {
                if (total == 0) {
                    return -1;
                } else {
                    return total;
                }
            } else {
                total+=read;
            }
        }
        return total;
    }

    @Override
    public long read(ByteBuffer[] dsts) throws IOException {
        return read(dsts, 0, dsts.length);
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        if (memory.length()-position <= 0) return -1;
        final int length = (int)Math.min(dst.remaining(), memory.length()-position);
        dst.limit(length);
        memory.getBytes(position, dst);
        position+=length;
        return length;
    }

    @Override
    public boolean isOpen() {
        return !closed;
    }

    @Override
    public void close() throws IOException {
        closed = true;
    }
}
