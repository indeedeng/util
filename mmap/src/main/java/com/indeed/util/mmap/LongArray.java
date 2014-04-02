package com.indeed.util.mmap;

import org.apache.log4j.Logger;

/**
 * @author jplaisance
 */
public final class LongArray {
    private static final Logger log = Logger.getLogger(LongArray.class);

    private static final long TYPE_SIZE = 8;

    private final Memory buffer;
    private final long length;

    public LongArray(Memory buffer, long address, long length) {
        if (address < 0) throw new IndexOutOfBoundsException("address must be >= 0");
        if (length < 0) throw new IllegalArgumentException("length must be >= 0");
        if (address+length*TYPE_SIZE > buffer.length()) throw new IndexOutOfBoundsException(String.format("address+length*%d must be <= buffer.length()", TYPE_SIZE));
        this.buffer = buffer.slice(address, length*TYPE_SIZE);
        this.length = length;
    }

    public long get(final long index) {
        return buffer.getLong(index*TYPE_SIZE);
    }

    public void get(final long index, final long[] longs, final int start, final int length) {
        for (int i = 0; i < length; i++) {
            longs[start+i] = buffer.getLong((index+i)*TYPE_SIZE);
        }
    }

    public void get(final long index, final long[] longs) {
        get(index, longs, 0, longs.length);
    }

    public void get(final long index, final LongArray longs, final long start, final long length) {
        longs.set(start, this, index, length);
    }

    public void get(final long index, final LongArray longs) {
        get(index, longs, 0, longs.length);
    }

    public void set(final long index, final long value) {
        buffer.putLong(index*TYPE_SIZE, value);
    }

    public void set(final long index, final long[] longs, final int start, final int length) {
        for (int i = 0; i < length; i++) {
            buffer.putLong((index+i)*TYPE_SIZE, longs[start+i]);
        }
    }

    public void set(final long index, final long[] longs) {
        set(index, longs, 0, longs.length);
    }

    public void set(final long index, final LongArray longs, final long start, final long length) {
        longs.buffer.getBytes(index*TYPE_SIZE, this.buffer, start*TYPE_SIZE, length*TYPE_SIZE);
    }

    public void set(final long index, final LongArray longs) {
        set(index, longs, 0, longs.length);
    }

    public long length() {
        return length;
    }

    public LongArray slice(final long start, final long length) {
        return new LongArray(buffer, start*TYPE_SIZE, length);
    }
}
