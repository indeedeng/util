package com.indeed.util.mmap;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * @author jplaisance
 */
final class ReverseEndianDirectDataAccess implements DirectDataAccess {
    private static final Logger log = LogManager.getLogger(ReverseEndianDirectDataAccess.class);

    private static final NativeEndianDirectDataAccess delegate = NativeEndianDirectDataAccess.getInstance();

    private static final ReverseEndianDirectDataAccess instance = new ReverseEndianDirectDataAccess();

    public static ReverseEndianDirectDataAccess getInstance() {
        return instance;
    }

    private ReverseEndianDirectDataAccess() {}

    @Override
    public byte getByte(long address) {
        return delegate.getByte(address);
    }

    @Override
    public char getChar(long address) {
        return Character.reverseBytes(delegate.getChar(address));
    }

    @Override
    public short getShort(long address) {
        return Short.reverseBytes(delegate.getShort(address));
    }

    @Override
    public int getInt(long address) {
        return Integer.reverseBytes(delegate.getInt(address));
    }

    @Override
    public long getLong(long address) {
        return Long.reverseBytes(delegate.getLong(address));
    }

    @Override
    public float getFloat(long address) {
        return Float.intBitsToFloat(Integer.reverseBytes(delegate.getInt(address)));
    }

    @Override
    public double getDouble(long address) {
        return Double.longBitsToDouble(Long.reverseBytes(delegate.getLong(address)));
    }

    @Override
    public void putByte(long address, byte val) {
        delegate.putByte(address, val);
    }

    @Override
    public void putChar(long address, char val) {
        delegate.putChar(address, Character.reverseBytes(val));
    }

    @Override
    public void putShort(long address, short val) {
        delegate.putShort(address, Short.reverseBytes(val));
    }

    @Override
    public void putInt(long address, int val) {
        delegate.putInt(address, Integer.reverseBytes(val));
    }

    @Override
    public void putLong(long address, long val) {
        delegate.putLong(address, Long.reverseBytes(val));
    }

    @Override
    public void putFloat(long address, float val) {
        delegate.putInt(address, Integer.reverseBytes(Float.floatToRawIntBits(val)));
    }

    @Override
    public void putDouble(long address, double val) {
        delegate.putLong(address, Long.reverseBytes(Double.doubleToRawLongBits(val)));
    }

    @Override
    public void copyMemory(long source, long dest, long length) {
        delegate.copyMemory(source, dest, length);
    }
}
