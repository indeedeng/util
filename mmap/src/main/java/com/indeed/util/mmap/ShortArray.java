package com.indeed.util.mmap;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * @author jplaisance
 */
public final class ShortArray {
    private static final Logger log = LogManager.getLogger(ShortArray.class);

    private static final long TYPE_SIZE = 2;

    private final Memory buffer;
    private final long length;

    public ShortArray(Memory buffer, long address, long length) {
        if (address < 0) throw new IndexOutOfBoundsException("address must be >= 0");
        if (length < 0) throw new IllegalArgumentException("length must be >= 0");
        if (address+length*TYPE_SIZE > buffer.length()) throw new IndexOutOfBoundsException(String.format("address+length*%d must be <= buffer.length()", TYPE_SIZE));
        this.buffer = buffer.slice(address, length*TYPE_SIZE);
        this.length = length;
    }

    public short get(final long index) {
        return buffer.getShort(index*TYPE_SIZE);
    }

    public void get(final long index, final short[] shorts, final int start, final int length) {
        for (int i = 0; i < length; i++) {
            shorts[start+i] = buffer.getShort((index+i)*TYPE_SIZE);
        }
    }

    public void get(final long index, final short[] shorts) {
        get(index, shorts, 0, shorts.length);
    }

    public void get(final long index, final ShortArray shorts, final long start, final long length) {
        shorts.set(start, this, index, length);
    }

    public void get(final long index, final ShortArray shorts) {
        get(index, shorts, 0, shorts.length);
    }

    public void set(final long index, final short value) {
        buffer.putShort(index*TYPE_SIZE, value);
    }

    public void set(final long index, final short[] shorts, final int start, final int length) {
        for (int i = 0; i < length; i++) {
            buffer.putShort((index+i)*TYPE_SIZE, shorts[start+i]);
        }
    }

    public void set(final long index, final short[] shorts) {
        set(index, shorts, 0, shorts.length);
    }

    public void set(final long index, final ShortArray shorts, final long start, final long length) {
        shorts.buffer.getBytes(index*TYPE_SIZE, this.buffer, start*TYPE_SIZE, length*TYPE_SIZE);
    }

    public void set(final long index, final ShortArray shorts) {
        set(index, shorts, 0, shorts.length);
    }

    public long length() {
        return length;
    }

    public ShortArray slice(final long start, final long length) {
        return new ShortArray(buffer, start*TYPE_SIZE, length);
    }
}
