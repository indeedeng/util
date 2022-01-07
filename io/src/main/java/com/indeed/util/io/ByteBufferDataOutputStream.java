package com.indeed.util.io;

import com.google.common.base.Throwables;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/** @author jplaisance */
public final class ByteBufferDataOutputStream extends OutputStream
        implements DataOutput, Positioned {

    private ByteBuffer buffer;
    private final boolean direct;
    private final DataOutputStream dataOutputStream = new DataOutputStream(this);

    public ByteBufferDataOutputStream() {
        this(128);
    }

    public ByteBufferDataOutputStream(int initialSize) {
        this(initialSize, false);
    }

    public ByteBufferDataOutputStream(int initialSize, boolean direct) {
        this.direct = direct;
        if (direct) {
            buffer = ByteBuffer.allocateDirect(initialSize);
        } else {
            buffer = ByteBuffer.allocate(initialSize);
        }
    }

    public void write(final int b) {
        ensureCapacity(1);
        buffer.put((byte) b);
    }

    public void write(final byte[] b, final int off, final int len) {
        ensureCapacity(len);
        buffer.put(b, off, len);
    }

    public void write(final byte[] b) {
        write(b, 0, b.length);
    }

    public void flush() {}

    public void close() {}

    public void writeBoolean(final boolean v) {
        ensureCapacity(1);
        buffer.put((byte) (v ? 1 : 0));
    }

    public void writeByte(final int v) {
        ensureCapacity(1);
        buffer.put((byte) v);
    }

    public void writeShort(final int v) {
        ensureCapacity(2);
        buffer.putShort((short) v);
    }

    public void writeChar(final int v) {
        ensureCapacity(2);
        buffer.putChar((char) v);
    }

    public void writeInt(final int v) {
        ensureCapacity(4);
        buffer.putInt(v);
    }

    public void writeLong(final long v) {
        ensureCapacity(8);
        buffer.putLong(v);
    }

    public void writeFloat(final float v) {
        ensureCapacity(4);
        buffer.putFloat(v);
    }

    public void writeDouble(final double v) {
        ensureCapacity(8);
        buffer.putDouble(v);
    }

    public void writeBytes(final String s) {
        try {
            dataOutputStream.writeBytes(s);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    public void writeChars(final String s) {
        try {
            dataOutputStream.writeChars(s);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    public void writeUTF(final String s) {
        try {
            dataOutputStream.writeUTF(s);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    private void ensureCapacity(int size) {
        if (buffer.remaining() < size) {
            final int newSize = Math.max(buffer.capacity() * 2, buffer.capacity() + size);
            final ByteBuffer newBuffer =
                    direct ? ByteBuffer.allocateDirect(newSize) : ByteBuffer.allocate(newSize);
            buffer.flip();
            newBuffer.put(buffer);
            buffer = newBuffer;
        }
    }

    public ByteBuffer getBuffer() {
        final ByteBuffer duplicate = buffer.duplicate();
        duplicate.flip();
        return duplicate.slice().asReadOnlyBuffer();
    }

    public ByteBuffer getBufferUnsafe() {
        return buffer;
    }

    public long position() throws IOException {
        return buffer.position();
    }

    public void clear() {
        buffer.clear();
    }
}
