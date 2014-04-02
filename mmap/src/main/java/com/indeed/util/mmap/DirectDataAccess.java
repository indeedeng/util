package com.indeed.util.mmap;

/**
 * @author jplaisance
 */
public interface DirectDataAccess {

    public byte getByte(long address);

    public char getChar(long address);

    public short getShort(long address);

    public int getInt(long address);

    public float getFloat(long address);

    public long getLong(long address);

    public double getDouble(long address);

    public void putByte(long address, byte val);

    public void putChar(long address, char val);

    public void putShort(long address, short val);

    public void putInt(long address, int val);

    public void putFloat(long address, float val);

    public void putLong(long address, long val);

    public void putDouble(long address, double val);

    public void copyMemory(long source, long dest, long length);
}
