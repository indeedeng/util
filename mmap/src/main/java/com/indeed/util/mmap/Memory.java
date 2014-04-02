package com.indeed.util.mmap;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author jplaisance
 */
public interface Memory {
    
    public byte getByte(final long l);

    public void putByte(final long l, final byte b);

    public short getShort(final long l);

    public void putShort(final long l, final short i);

    public char getChar(final long l);

    public void putChar(final long l, final char c);

    public int getInt(final long l);

    public void putInt(final long l, final int i);

    public long getLong(final long l);

    public void putLong(final long l, final long l1);

    public float getFloat(final long l);

    public void putFloat(final long l, final float v);

    public double getDouble(final long l);

    public void putDouble(final long l, final double v);

    public void putBytes(final long l, final byte[] bytes);

    public void putBytes(final long l, final byte[] bytes, final int start, final int length);
    
    public void putBytes(final long l, final Memory source);
    
    public void putBytes(final long l, final Memory source, final long start, final long length);

    public void putBytes(final long l, final ByteBuffer source);

    public void getBytes(final long l, final byte[] bytes);

    public void getBytes(final long l, final byte[] bytes, final int start, final int length);
    
    public void getBytes(final long l, final Memory dest);

    public void getBytes(final long l, final Memory dest, final long start, final long length);
    
    public void getBytes(final long l, final ByteBuffer dest);

    public Memory slice(long startAddress, long sliceLength);

    public ByteArray byteArray(long start, long numBytes);

    public ShortArray shortArray(long start, long numShorts);

    public IntArray intArray(long start, long numInts);

    public LongArray longArray(long start, long numLongs);

    public FloatArray floatArray(long start, long numFloats);

    public DoubleArray doubleArray(long start, long numDoubles);

    public CharArray charArray(long start, long numChars);

    public long length();

    public boolean isDirect();
    
    public ByteOrder getOrder();
}
