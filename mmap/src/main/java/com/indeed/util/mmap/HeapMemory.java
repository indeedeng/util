package com.indeed.util.mmap;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author jplaisance
 */
public final class HeapMemory extends AbstractMemory {
    private static final Logger log = LogManager.getLogger(HeapMemory.class);
    private static final boolean debug = true;

    private final byte[] data;
    private final int offset;
    private final int length;
    private final HeapDataAccess dataAccess;
    private final ByteOrder endianness;

    public HeapMemory(int length, ByteOrder endianness) {
        this(new byte[length], endianness);
    }

    public HeapMemory(byte[] data, ByteOrder endianness) {
        this(data, 0, data.length, endianness);
    }

    public HeapMemory(byte[] data, int offset, int length, ByteOrder endianness) {
        this.data = data;
        this.offset = offset;
        this.length = length;
        this.endianness = endianness;
        if (endianness == ByteOrder.nativeOrder()) {
            dataAccess = NativeEndianHeapDataAccess.getInstance();
        } else {
            dataAccess = ReverseEndianHeapDataAccess.getInstance();
        }
    }

    private void checkBounds(long l, long length) {
        if (debug) {
            if (l < 0 || l > this.length-length) throw new IndexOutOfBoundsException(String.valueOf(l));
        }
    }

    private void checkArrayBounds(final byte[] bytes, final int start, final int length) {
        if (debug) {
            if (bytes == null) throw new IllegalArgumentException("byte[] bytes cannot be null");
            if (start < 0) throw new IndexOutOfBoundsException("start cannot be less than zero");
            if (length < 0) throw new IllegalArgumentException("length cannot be less than zero");
            if (start+length > bytes.length) throw new IndexOutOfBoundsException("start plus length cannot be greater than length of byte[] bytes");
            if (start+length < 0) throw new IndexOutOfBoundsException("start plus length cannot be greater than Integer.MAX_VALUE");
        }
    }

    @Override
    public byte getByte(long l) {
        checkBounds(l, 1);
        return data[(int)(offset+l)];
    }

    @Override
    public void putByte(long l, byte b) {
        checkBounds(l, 1);
        data[(int)(offset+l)] = b;
    }

    @Override
    public short getShort(long l) {
        checkBounds(l, 2);
        return dataAccess.getShort(data, (int)(offset+l));
    }

    @Override
    public void putShort(long l, short i) {
        checkBounds(l, 2);
        dataAccess.putShort(data, (int)(offset+l), i);
    }

    @Override
    public char getChar(long l) {
        checkBounds(l, 2);
        return dataAccess.getChar(data, (int)(offset+l));
    }

    @Override
    public void putChar(long l, char c) {
        checkBounds(l, 2);
        dataAccess.putChar(data, (int)(offset+l), c);
    }

    @Override
    public int getInt(long l) {
        checkBounds(l, 4);
        return dataAccess.getInt(data, (int)(offset+l));
    }

    @Override
    public void putInt(long l, int i) {
        checkBounds(l, 4);
        dataAccess.putInt(data, (int)(offset+l), i);
    }

    @Override
    public long getLong(long l) {
        checkBounds(l, 8);
        return dataAccess.getLong(data, (int)(offset+l));
    }

    @Override
    public void putLong(long l, long l1) {
        checkBounds(l, 8);
        dataAccess.putLong(data, (int)(offset+l), l1);
    }

    @Override
    public float getFloat(long l) {
        checkBounds(l, 4);
        return dataAccess.getFloat(data, (int)(offset+l));
    }

    @Override
    public void putFloat(long l, float v) {
        checkBounds(l, 4);
        dataAccess.putFloat(data, (int)(offset+l), v);
    }

    @Override
    public double getDouble(long l) {
        checkBounds(l, 8);
        return dataAccess.getDouble(data, (int)(offset+l));
    }

    @Override
    public void putDouble(long l, double v) {
        checkBounds(l, 8);
        dataAccess.putDouble(data, (int)(offset+l), v);
    }

    @Override
    public void putBytes(long l, byte[] bytes) {
        putBytes(l, bytes, 0, bytes.length);
    }

    @Override
    public void putBytes(long l, byte[] bytes, int start, int length) {
        checkBounds(l, length);
        checkArrayBounds(bytes, start, length);
        System.arraycopy(bytes, start, data, (int)(offset+l), length);
    }

    @Override
    public void putBytes(long l, Memory source) {
        putBytes(l, source, 0, source.length());
    }

    @Override
    public void putBytes(long l, Memory source, long start, long length) {
        checkBounds(l, length);
        source.getBytes(start, data, (int)(offset+l), (int)length);
    }

    @Override
    public void putBytes(long l, ByteBuffer source) {
        final int length = source.remaining();
        checkBounds(l, length);
        source.get(data, (int) (offset+l), length);
    }

    @Override
    public void getBytes(long l, byte[] bytes) {
        getBytes(l, bytes, 0, bytes.length);
    }

    @Override
    public void getBytes(long l, byte[] bytes, int start, int length) {
        checkBounds(l, length);
        checkArrayBounds(bytes, start, length);
        System.arraycopy(data, (int)(offset+l), bytes, start, length);
    }

    @Override
    public void getBytes(long l, Memory dest) {
        getBytes(l, dest, 0, dest.length());
    }

    @Override
    public void getBytes(long l, Memory dest, long start, long length) {
        checkBounds(l, length);
        dest.putBytes(start, data, (int)(offset+l), (int)length);
    }

    @Override
    public void getBytes(long l, ByteBuffer dest) {
        final int length = dest.remaining();
        checkBounds(l, length);
        dest.put(data, (int)(offset+l), length);
    }

    @Override
    public Memory slice(long startAddress, long sliceLength) {
        if (startAddress < 0) throw new IllegalArgumentException("startAddress must be >= 0");
        if (sliceLength < 0) throw new IllegalArgumentException("sliceLength must be >= 0");
        if (startAddress+sliceLength > length) throw new IllegalArgumentException("startAddress+sliceLength must be <= length");
        return new HeapMemory(data, (int)(offset+startAddress), (int)sliceLength, endianness);
    }

    @Override
    public long length() {
        return length;
    }

    @Override
    public boolean isDirect() {
        return false;
    }

    @Override
    public ByteOrder getOrder() {
        return endianness;
    }
}
