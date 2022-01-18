package com.indeed.util.mmap;

import com.indeed.util.unsafe.IndeedUnsafe;

import static com.indeed.util.unsafe.IndeedUnsafe.BYTE_ARRAY_BASE_OFFSET;

/**
 * Not bounds checked. Should be bounds checked externally.
 *
 * @author jplaisance
 */
final class NativeEndianHeapDataAccess implements HeapDataAccess {

    private static final NativeEndianHeapDataAccess instance = new NativeEndianHeapDataAccess();

    public static NativeEndianHeapDataAccess getInstance() {
        return instance;
    }

    private NativeEndianHeapDataAccess() {}

    @Override
    public char getChar(byte[] b, int off) {
        return IndeedUnsafe.getChar(b, BYTE_ARRAY_BASE_OFFSET + off);
    }

    @Override
    public short getShort(byte[] b, int off) {
        return IndeedUnsafe.getShort(b, BYTE_ARRAY_BASE_OFFSET + off);
    }

    @Override
    public int getInt(byte[] b, int off) {
        return IndeedUnsafe.getInt(b, BYTE_ARRAY_BASE_OFFSET + off);
    }

    @Override
    public float getFloat(byte[] b, int off) {
        return IndeedUnsafe.getFloat(b, BYTE_ARRAY_BASE_OFFSET + off);
    }

    @Override
    public long getLong(byte[] b, int off) {
        return IndeedUnsafe.getLong(b, BYTE_ARRAY_BASE_OFFSET + off);
    }

    @Override
    public double getDouble(byte[] b, int off) {
        return IndeedUnsafe.getDouble(b, BYTE_ARRAY_BASE_OFFSET + off);
    }

    @Override
    public void putChar(byte[] b, int off, char val) {
        IndeedUnsafe.putChar(b, BYTE_ARRAY_BASE_OFFSET + off, val);
    }

    @Override
    public void putShort(byte[] b, int off, short val) {
        IndeedUnsafe.putShort(b, BYTE_ARRAY_BASE_OFFSET + off, val);
    }

    @Override
    public void putInt(byte[] b, int off, int val) {
        IndeedUnsafe.putInt(b, BYTE_ARRAY_BASE_OFFSET + off, val);
    }

    @Override
    public void putFloat(byte[] b, int off, float val) {
        IndeedUnsafe.putFloat(b, BYTE_ARRAY_BASE_OFFSET + off, val);
    }

    @Override
    public void putLong(byte[] b, int off, long val) {
        IndeedUnsafe.putLong(b, BYTE_ARRAY_BASE_OFFSET + off, val);
    }

    @Override
    public void putDouble(byte[] b, int off, double val) {
        IndeedUnsafe.putDouble(b, BYTE_ARRAY_BASE_OFFSET + off, val);
    }
}
