package com.indeed.util.mmap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author jplaisance */
final class ReverseEndianHeapDataAccess implements HeapDataAccess {
    private static final Logger log = LoggerFactory.getLogger(ReverseEndianHeapDataAccess.class);
    private static final ReverseEndianHeapDataAccess instance = new ReverseEndianHeapDataAccess();

    private static final NativeEndianHeapDataAccess delegate =
            NativeEndianHeapDataAccess.getInstance();

    public static ReverseEndianHeapDataAccess getInstance() {
        return instance;
    }

    private ReverseEndianHeapDataAccess() {}

    @Override
    public char getChar(byte[] b, int off) {
        return Character.reverseBytes(delegate.getChar(b, off));
    }

    @Override
    public short getShort(byte[] b, int off) {
        return Short.reverseBytes(delegate.getShort(b, off));
    }

    @Override
    public int getInt(byte[] b, int off) {
        return Integer.reverseBytes(delegate.getInt(b, off));
    }

    @Override
    public float getFloat(byte[] b, int off) {
        return Float.intBitsToFloat(getInt(b, off));
    }

    @Override
    public long getLong(byte[] b, int off) {
        return Long.reverseBytes(delegate.getLong(b, off));
    }

    @Override
    public double getDouble(byte[] b, int off) {
        return Double.longBitsToDouble(getLong(b, off));
    }

    @Override
    public void putChar(byte[] b, int off, char val) {
        delegate.putChar(b, off, Character.reverseBytes(val));
    }

    @Override
    public void putShort(byte[] b, int off, short val) {
        delegate.putShort(b, off, Short.reverseBytes(val));
    }

    @Override
    public void putInt(byte[] b, int off, int val) {
        delegate.putInt(b, off, Integer.reverseBytes(val));
    }

    @Override
    public void putFloat(byte[] b, int off, float val) {
        putInt(b, off, Float.floatToRawIntBits(val));
    }

    @Override
    public void putLong(byte[] b, int off, long val) {
        delegate.putLong(b, off, Long.reverseBytes(val));
    }

    @Override
    public void putDouble(byte[] b, int off, double val) {
        putLong(b, off, Double.doubleToRawLongBits(val));
    }
}
