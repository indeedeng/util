package com.indeed.util.mmap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author jplaisance */
public final class DoubleArray {
    private static final Logger log = LoggerFactory.getLogger(DoubleArray.class);

    private static final long TYPE_SIZE = 8;

    private final Memory buffer;
    private final long length;

    public DoubleArray(Memory buffer, long address, long length) {
        if (address < 0) throw new IndexOutOfBoundsException("address must be >= 0");
        if (length < 0) throw new IllegalArgumentException("length must be >= 0");
        if (address + length * TYPE_SIZE > buffer.length())
            throw new IndexOutOfBoundsException(
                    String.format("address+length*%d must be <= buffer.length()", TYPE_SIZE));
        this.buffer = buffer.slice(address, length * TYPE_SIZE);
        this.length = length;
    }

    public double get(final long index) {
        return buffer.getDouble(index * TYPE_SIZE);
    }

    public void get(final long index, final double[] doubles, final int start, final int length) {
        for (int i = 0; i < length; i++) {
            doubles[start + i] = buffer.getDouble((index + i) * TYPE_SIZE);
        }
    }

    public void get(final long index, final double[] doubles) {
        get(index, doubles, 0, doubles.length);
    }

    public void get(
            final long index, final DoubleArray doubles, final long start, final long length) {
        doubles.set(start, this, index, length);
    }

    public void get(final long index, final DoubleArray doubles) {
        get(index, doubles, 0, doubles.length);
    }

    public void set(final long index, final double value) {
        buffer.putDouble(index * TYPE_SIZE, value);
    }

    public void set(final long index, final double[] doubles, final int start, final int length) {
        for (int i = 0; i < length; i++) {
            buffer.putDouble((index + i) * TYPE_SIZE, doubles[start + i]);
        }
    }

    public void set(final long index, final double[] doubles) {
        set(index, doubles, 0, doubles.length);
    }

    public void set(
            final long index, final DoubleArray doubles, final long start, final long length) {
        doubles.buffer.getBytes(
                index * TYPE_SIZE, this.buffer, start * TYPE_SIZE, length * TYPE_SIZE);
    }

    public void set(final long index, final DoubleArray doubles) {
        set(index, doubles, 0, doubles.length);
    }

    public long length() {
        return length;
    }

    public DoubleArray slice(final long start, final long length) {
        return new DoubleArray(buffer, start * TYPE_SIZE, length);
    }
}
