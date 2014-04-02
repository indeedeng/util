package com.indeed.util.mmap;

import com.google.common.io.ByteStreams;
import org.apache.log4j.Logger;

import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteOrder;

/**
 * Zero Copy is sort of a lie, it's zero copy if realloc decides not to copy for sizes less than MMAP_THRESHOLD (default 256 k) and
 * then after that it's totally zero copy
 *
 * @author jplaisance
 */
public final class ZeroCopyOutputStream extends OutputStream implements DataOutput {
    private static final Logger log = Logger.getLogger(ZeroCopyOutputStream.class);

    private NativeBuffer buffer;
    private DirectMemory memory;

    public ZeroCopyOutputStream() {
        this(65536, ByteOrder.BIG_ENDIAN);
    }

    public ZeroCopyOutputStream(int initialSize, ByteOrder order) {
        buffer = new NativeBuffer(initialSize, order);
        memory = buffer.memory();
    }

    private long currentAddress = 0;

    public void writeBoolean(final boolean v) throws IOException {
        if (currentAddress + 1 > memory.length()) {
            buffer = buffer.realloc(memory.length()*2);
            memory = buffer.memory();
            writeBoolean(v);
        } else {
            memory.putByte(currentAddress, (byte)(v ? 1 : 0));
            currentAddress++;
        }
    }

    public void writeByte(final int v) throws IOException {
        if (currentAddress + 1 > memory.length()) {
            buffer = buffer.realloc(memory.length()*2);
            memory = buffer.memory();
            writeByte(v);
        } else {
            memory.putByte(currentAddress, (byte)v);
            currentAddress++;
        }
    }

    public void writeShort(final int v) throws IOException {
        if (currentAddress + 2 > memory.length()) {
            buffer = buffer.realloc(memory.length()*2);
            memory = buffer.memory();
            writeShort(v);
        } else {
            memory.putShort(currentAddress, (short)v);
            currentAddress+=2;
        }
    }

    public void writeChar(final int v) throws IOException {
        if (currentAddress + 2 > memory.length()) {
            buffer = buffer.realloc(memory.length()*2);
            memory = buffer.memory();
            writeChar(v);
        } else {
            memory.putChar(currentAddress, (char)v);
            currentAddress+=2;
        }
    }

    public void writeInt(final int v) throws IOException {
        if (currentAddress + 4 > memory.length()) {
            buffer = buffer.realloc(memory.length()*2);
            memory = buffer.memory();
            writeInt(v);
        } else {
            memory.putInt(currentAddress, v);
            currentAddress+=4;
        }
    }

    public void writeLong(final long v) throws IOException {
        if (currentAddress + 8 > memory.length()) {
            buffer = buffer.realloc(memory.length()*2);
            memory = buffer.memory();
            writeLong(v);
        } else {
            memory.putLong(currentAddress, v);
            currentAddress+=8;
        }
    }

    public void writeFloat(final float v) throws IOException {
        if (currentAddress + 4 > memory.length()) {
            buffer = buffer.realloc(memory.length()*2);
            memory = buffer.memory();
            writeFloat(v);
        } else {
            memory.putFloat(currentAddress, v);
            currentAddress+=4;
        }
    }

    public void writeDouble(final double v) throws IOException {
        if (currentAddress + 8 > memory.length()) {
            buffer = buffer.realloc(memory.length()*2);
            memory = buffer.memory();
            writeDouble(v);
        } else {
            memory.putDouble(currentAddress, v);
            currentAddress+=8;
        }
    }

    public void writeBytes(final String s) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void writeChars(final String s) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void writeUTF(final String s) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void write(final int b) throws IOException {
        writeByte(b);
    }

    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        if (off < 0 || len < 0 || off+len > b.length) {
            throw new IndexOutOfBoundsException();
        }
        if (currentAddress + len > memory.length()) {
            buffer = buffer.realloc(memory.length()*2);
            memory = buffer.memory();
            write(b, off, len);
        } else {
            memory.putBytes(currentAddress, b, off, len);
            currentAddress+=len;
        }
    }

    @Override
    public void close() throws IOException {
        buffer.close();
    }

    public long position() {
        return currentAddress;
    }

    //the memory object returned by this call is invalidated by closing or writing additional data to this output stream
    public DirectMemory memory() {
        return memory.slice(0, currentAddress);
    }

    public void writeTo(OutputStream outputStream) throws IOException {
        final MemoryInputStream in = new MemoryInputStream(memory());
        ByteStreams.copy(in, outputStream);
    }

    //the input stream returned by this call is invalidated by closing or writing additional data to this output stream
    public InputStream getInputStream() {
        return new MemoryInputStream(memory());
    }
}
