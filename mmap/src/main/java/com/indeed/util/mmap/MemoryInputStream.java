package com.indeed.util.mmap;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author jplaisance
 */
public final class MemoryInputStream extends InputStream {
    private static final Logger log = Logger.getLogger(MemoryInputStream.class);

    private final Memory memory;

    private long position = 0;

    public MemoryInputStream(Memory memory) {
        this.memory = memory;
    }

    @Override
    public int read() throws IOException {
        if (position >= length()) {
            return -1;
        }
        return memory.getByte(position++)&0xFF;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        //min of int and long is guaranteed to be int
        final int read = (int)Math.min(len, length()-position);
        if (read == 0) return -1;
        memory.getBytes(position, b, off, read);
        position+=read;
        return read;
    }

    @Override
    public long skip(long n) throws IOException {
        position+=n;
        return n;
    }

    public void seek(long position) {
        if (position < 0 || position > length()) throw new IndexOutOfBoundsException(String.valueOf(position));
        this.position = position;
    }

    public long getPos() {
        return position;
    }

    public long length() {
        return memory.length();
    }
}
