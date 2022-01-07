package com.indeed.util.mmap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author jplaisance */
public final class ByteArray {
    private static final Logger log = LoggerFactory.getLogger(ByteArray.class);

    private final Memory buffer;
    private final long length;

    public ByteArray(Memory buffer, long address, long length) {
        if (address < 0) throw new IndexOutOfBoundsException("address must be >= 0");
        if (length < 0) throw new IllegalArgumentException("length must be >= 0");
        if (address + length > buffer.length())
            throw new IndexOutOfBoundsException("address+length must be <= buffer.length()");
        this.buffer = buffer.slice(address, length);
        this.length = length;
    }

    public byte get(final long index) {
        return buffer.getByte(index);
    }

    public void get(final long index, final byte[] bytes, final int start, final int length) {
        buffer.getBytes(index, bytes, start, length);
    }

    public void get(final long index, final ByteArray bytes, final long start, final long length) {
        bytes.set(start, this, index, length);
    }

    public void get(final long index, final ByteArray bytes) {
        get(index, bytes, 0, bytes.length);
    }

    public void get(final long index, final byte[] bytes) {
        get(index, bytes, 0, bytes.length);
    }

    public void set(final long index, final byte value) {
        buffer.putByte(index, value);
    }

    public void set(final long index, final byte[] bytes, final int start, final int length) {
        buffer.putBytes(index, bytes, start, length);
    }

    public void set(final long index, final byte[] bytes) {
        set(index, bytes, 0, bytes.length);
    }

    public void set(final long index, final ByteArray bytes, final long start, final long length) {
        bytes.buffer.getBytes(index, this.buffer, start, length);
    }

    public void set(final long index, final ByteArray bytes) {
        set(index, bytes, 0, bytes.length);
    }

    public long length() {
        return length;
    }

    public ByteArray slice(final long start, final long length) {
        return new ByteArray(buffer, start, length);
    }
}
