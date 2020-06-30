package com.indeed.util.mmap;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * @author jplaisance
 */
public final class IntArray {
    private static final Logger log = LogManager.getLogger(IntArray.class);

    private static final long TYPE_SIZE = 4;

    private final Memory buffer;
    private final long length;

    public IntArray(Memory buffer, long address, long length) {
        if (address < 0) throw new IndexOutOfBoundsException("address must be >= 0");
        if (length < 0) throw new IllegalArgumentException("length must be >= 0");
        if (address+length*TYPE_SIZE > buffer.length()) throw new IndexOutOfBoundsException(String.format("address+length*%d must be <= buffer.length()", TYPE_SIZE));
        this.buffer = buffer.slice(address, length*TYPE_SIZE);
        this.length = length;
    }

    public int get(final long index) {
        return buffer.getInt(index*TYPE_SIZE);
    }

    public void get(final long index, final int[] ints, final int start, final int length) {
        for (int i = 0; i < length; i++) {
            ints[start+i] = buffer.getInt((index+i)*TYPE_SIZE);
        }
    }

    public void get(final long index, final int[] ints) {
        get(index, ints, 0, ints.length);
    }
    
    public void get(final long index, final IntArray ints, final long start, final long length) {
        ints.set(start, this, index, length);
    }

    public void get(final long index, final IntArray ints) {
        get(index, ints, 0, ints.length);
    }

    public void set(final long index, final int value) {
        buffer.putInt(index*TYPE_SIZE, value);
    }

    public void set(final long index, final int[] ints, final int start, final int length) {
        for (int i = 0; i < length; i++) {
            buffer.putInt((index+i)*TYPE_SIZE, ints[start+i]);
        }
    }

    public void set(final long index, final int[] ints) {
        set(index, ints, 0, ints.length);
    }

    public void set(final long index, final IntArray ints, final long start, final long length) {
        ints.buffer.getBytes(index*TYPE_SIZE, this.buffer, start*TYPE_SIZE, length*TYPE_SIZE);
    }

    public void set(final long index, final IntArray ints) {
        set(index, ints, 0, ints.length);
    }

    public long length() {
        return length;
    }

    public IntArray slice(final long start, final long length) {
        return new IntArray(buffer, start*TYPE_SIZE, length);
    }
}
