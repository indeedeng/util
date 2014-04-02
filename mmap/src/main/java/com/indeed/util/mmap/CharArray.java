package com.indeed.util.mmap;

import org.apache.log4j.Logger;

/**
 * @author jplaisance
 */
public final class CharArray {
    private static final Logger log = Logger.getLogger(CharArray.class);

    private static final long TYPE_SIZE = 2;

    private final Memory buffer;
    private final long length;

    public CharArray(Memory buffer, long address, long length) {
        if (address < 0) throw new IndexOutOfBoundsException("address must be >= 0");
        if (length < 0) throw new IllegalArgumentException("length must be >= 0");
        if (address+length*TYPE_SIZE > buffer.length()) throw new IndexOutOfBoundsException(String.format("address+length*%d must be <= buffer.length()", TYPE_SIZE));
        this.buffer = buffer.slice(address, length*TYPE_SIZE);
        this.length = length;
    }

    public char get(final long index) {
        return buffer.getChar(index*TYPE_SIZE);
    }

    public void get(final long index, final char[] chars, final int start, final int length) {
        for (int i = 0; i < length; i++) {
            chars[start+i] = buffer.getChar((index+i)*TYPE_SIZE);
        }
    }

    public void get(final long index, final char[] chars) {
        get(index, chars, 0, chars.length);
    }

    public void get(final long index, final CharArray chars, final long start, final long length) {
        chars.set(start, this, index, length);
    }

    public void get(final long index, final CharArray chars) {
        get(index, chars, 0, chars.length);
    }

    public void set(final long index, final char value) {
        buffer.putChar(index*TYPE_SIZE, value);
    }

    public void set(final long index, final char[] chars, final int start, final int length) {
        for (int i = 0; i < length; i++) {
            buffer.putChar((index+i)*TYPE_SIZE, chars[start+i]);
        }
    }

    public void set(final long index, final char[] chars) {
        set(index, chars, 0, chars.length);
    }

    public void set(final long index, final CharArray chars, final long start, final long length) {
        chars.buffer.getBytes(index*TYPE_SIZE, this.buffer, start*TYPE_SIZE, length*TYPE_SIZE);
    }

    public void set(final long index, final CharArray chars) {
        set(index, chars, 0, chars.length);
    }

    public long length() {
        return length;
    }

    public CharArray slice(final long start, final long length) {
        return new CharArray(buffer, start*TYPE_SIZE, length);
    }
}
