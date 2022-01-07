package com.indeed.util.mmap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInput;
import java.io.IOException;

/**
 * DataInput for memory with same endianness as underlying memory.
 *
 * @author jplaisance
 */
public final class MemoryDataInput implements DataInput {

    private static final Logger log = LoggerFactory.getLogger(MemoryDataInput.class);

    private final Memory memory;

    private long position = 0;

    public MemoryDataInput(final Memory memory) {
        this.memory = memory;
    }

    @Override
    public void readFully(final byte[] b) throws IOException {
        readFully(b, 0, b.length);
    }

    @Override
    public void readFully(final byte[] b, final int off, final int len) throws IOException {
        memory.getBytes(position, b, off, len);
        position += len;
    }

    @Override
    public int skipBytes(final int n) throws IOException {
        position += n;
        return n;
    }

    @Override
    public boolean readBoolean() throws IOException {
        return memory.getByte(position++) != 0;
    }

    @Override
    public byte readByte() throws IOException {
        return memory.getByte(position++);
    }

    @Override
    public int readUnsignedByte() throws IOException {
        return readByte() & 0xFF;
    }

    @Override
    public short readShort() throws IOException {
        short ret = memory.getShort(position);
        position += 2;
        return ret;
    }

    @Override
    public int readUnsignedShort() throws IOException {
        return readShort() & 0xFFFF;
    }

    @Override
    public char readChar() throws IOException {
        char ret = memory.getChar(position);
        position += 2;
        return ret;
    }

    @Override
    public int readInt() throws IOException {
        int ret = memory.getInt(position);
        position += 4;
        return ret;
    }

    @Override
    public long readLong() throws IOException {
        long ret = memory.getLong(position);
        position += 8;
        return ret;
    }

    @Override
    public float readFloat() throws IOException {
        float ret = memory.getFloat(position);
        position += 4;
        return ret;
    }

    @Override
    public double readDouble() throws IOException {
        double ret = memory.getDouble(position);
        position += 8;
        return ret;
    }

    @Override
    public String readLine() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String readUTF() throws IOException {
        throw new UnsupportedOperationException();
    }

    public void seek(long position) {
        this.position = position;
    }

    public long position() {
        return position;
    }

    public long length() {
        return memory.length();
    }
}
