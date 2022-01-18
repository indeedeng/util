package com.indeed.util.mmap;

import com.indeed.util.unsafe.IndeedUnsafe;

/** @author jplaisance */
final class NativeEndianDirectDataAccess implements DirectDataAccess {

    private static final NativeEndianDirectDataAccess instance = new NativeEndianDirectDataAccess();

    public static NativeEndianDirectDataAccess getInstance() {
        return instance;
    }

    private NativeEndianDirectDataAccess() {}

    @Override
    public byte getByte(long address) {
        return IndeedUnsafe.getByte(address);
    }

    @Override
    public char getChar(long address) {
        return IndeedUnsafe.getChar(address);
    }

    @Override
    public short getShort(long address) {
        return IndeedUnsafe.getShort(address);
    }

    @Override
    public int getInt(long address) {
        return IndeedUnsafe.getInt(address);
    }

    @Override
    public float getFloat(long address) {
        return IndeedUnsafe.getFloat(address);
    }

    @Override
    public long getLong(long address) {
        return IndeedUnsafe.getLong(address);
    }

    @Override
    public double getDouble(long address) {
        return IndeedUnsafe.getDouble(address);
    }

    @Override
    public void putByte(long address, byte val) {
        IndeedUnsafe.putByte(address, val);
    }

    @Override
    public void putChar(long address, char val) {
        IndeedUnsafe.putChar(address, val);
    }

    @Override
    public void putShort(long address, short val) {
        IndeedUnsafe.putShort(address, val);
    }

    @Override
    public void putInt(long address, int val) {
        IndeedUnsafe.putInt(address, val);
    }

    @Override
    public void putFloat(long address, float val) {
        IndeedUnsafe.putFloat(address, val);
    }

    @Override
    public void putLong(long address, long val) {
        IndeedUnsafe.putLong(address, val);
    }

    @Override
    public void putDouble(long address, double val) {
        IndeedUnsafe.putDouble(address, val);
    }

    @Override
    public void copyMemory(long source, long dest, long length) {
        IndeedUnsafe.copyMemory(source, dest, length);
    }
}
