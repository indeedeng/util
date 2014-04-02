package com.indeed.util.mmap;

/**
 * @author jplaisance
 */
public interface HeapDataAccess {

    public char getChar(byte[] b, int off);

    public short getShort(byte[] b, int off);

    public int getInt(byte[] b, int off);

    public float getFloat(byte[] b, int off);

    public long getLong(byte[] b, int off);

    public double getDouble(byte[] b, int off);

    public void putChar(byte[] b, int off, char val);

    public void putShort(byte[] b, int off, short val);

    public void putInt(byte[] b, int off, int val);

    public void putFloat(byte[] b, int off, float val);

    public void putLong(byte[] b, int off, long val);

    public void putDouble(byte[] b, int off, double val);
}
