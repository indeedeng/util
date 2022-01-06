package com.indeed.util.mmap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jplaisance
 */
public final class FloatArray {
    private static final Logger log = LoggerFactory.getLogger(FloatArray.class);

    private static final long TYPE_SIZE = 4;

    private final Memory buffer;
    private final long length;

    public FloatArray(Memory buffer, long address, long length) {
        if (address < 0) throw new IndexOutOfBoundsException("address must be >= 0");
        if (length < 0) throw new IllegalArgumentException("length must be >= 0");
        if (address+length*TYPE_SIZE > buffer.length()) throw new IndexOutOfBoundsException(String.format("address+length*%d must be <= buffer.length()", TYPE_SIZE));
        this.buffer = buffer.slice(address, length*TYPE_SIZE);
        this.length = length;
    }

    public float get(final long index) {
        return buffer.getFloat(index*TYPE_SIZE);
    }

    public void get(final long index, final float[] floats, final int start, final int length) {
        for (int i = 0; i < length; i++) {
            floats[start+i] = buffer.getFloat((index+i)*TYPE_SIZE);
        }
    }

    public void get(final long index, final float[] floats) {
        get(index, floats, 0, floats.length);
    }

    public void get(final long index, final FloatArray floats, final long start, final long length) {
        floats.set(start, this, index, length);
    }

    public void get(final long index, final FloatArray floats) {
        get(index, floats, 0, floats.length);
    }

    public void set(final long index, final float value) {
        buffer.putFloat(index*TYPE_SIZE, value);
    }

    public void set(final long index, final float[] floats, final int start, final int length) {
        for (int i = 0; i < length; i++) {
            buffer.putFloat((index+i)*TYPE_SIZE, floats[start+i]);
        }
    }

    public void set(final long index, final float[] floats) {
        set(index, floats, 0, floats.length);
    }

    public void set(final long index, final FloatArray floats, final long start, final long length) {
        floats.buffer.getBytes(index*TYPE_SIZE, this.buffer, start*TYPE_SIZE, length*TYPE_SIZE);
    }

    public void set(final long index, final FloatArray floats) {
        set(index, floats, 0, floats.length);
    }

    public long length() {
        return length;
    }

    public FloatArray slice(final long start, final long length) {
        return new FloatArray(buffer, start*TYPE_SIZE, length);
    }
}
