package com.indeed.util.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.util.Arrays;

/** @author jplaisance */
public final class UnsafeByteArrayOutputStream extends OutputStream {
    private static final Logger log = LoggerFactory.getLogger(UnsafeByteArrayOutputStream.class);

    private byte[] buf;
    private int count = 0;

    public UnsafeByteArrayOutputStream() {
        this(32);
    }

    public UnsafeByteArrayOutputStream(int initialCapacity) {
        buf = new byte[initialCapacity];
    }

    public void write(int b) {
        int newcount = count + 1;
        if (newcount > buf.length) {
            buf = Arrays.copyOf(buf, Math.max(buf.length << 1, newcount));
        }
        buf[count] = (byte) b;
        count = newcount;
    }

    public void write(byte b[], int off, int len) {
        if ((off < 0)
                || (off > b.length)
                || (len < 0)
                || ((off + len) > b.length)
                || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        int newcount = count + len;
        if (newcount > buf.length) {
            buf = Arrays.copyOf(buf, Math.max(buf.length << 1, newcount));
        }
        System.arraycopy(b, off, buf, count, len);
        count = newcount;
    }

    public byte[] getByteArray() {
        return buf;
    }

    public int size() {
        return count;
    }

    public void reset() {
        count = 0;
    }
}
