package com.indeed.util.mmap;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * Not bounds checked. Should be bounds checked externally.
 * @author jplaisance
 */
final class NativeEndianHeapDataAccess implements HeapDataAccess {
    private static final Logger log = LogManager.getLogger(NativeEndianHeapDataAccess.class);

    private static final Unsafe UNSAFE;
    private static final long BYTE_ARRAY_BASE_OFFSET;
    private static final NativeEndianHeapDataAccess instance = new NativeEndianHeapDataAccess();

    public static NativeEndianHeapDataAccess getInstance() {
        return instance;
    }

    private NativeEndianHeapDataAccess() {}

    static {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            UNSAFE = (Unsafe)theUnsafe.get(null);
            BYTE_ARRAY_BASE_OFFSET = UNSAFE.arrayBaseOffset(byte[].class);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public char getChar(byte[] b, int off) {
        return UNSAFE.getChar(b, BYTE_ARRAY_BASE_OFFSET + off);
    }

    @Override
    public short getShort(byte[] b, int off) {
        return UNSAFE.getShort(b, BYTE_ARRAY_BASE_OFFSET + off);
    }

    @Override
    public int getInt(byte[] b, int off) {
        return UNSAFE.getInt(b, BYTE_ARRAY_BASE_OFFSET + off);
    }

    @Override
    public float getFloat(byte[] b, int off) {
        return UNSAFE.getFloat(b, BYTE_ARRAY_BASE_OFFSET + off);
    }

    @Override
    public long getLong(byte[] b, int off) {
        return UNSAFE.getLong(b, BYTE_ARRAY_BASE_OFFSET + off);
    }

    @Override
    public double getDouble(byte[] b, int off) {
        return UNSAFE.getDouble(b, BYTE_ARRAY_BASE_OFFSET+off);
    }

    @Override
    public void putChar(byte[] b, int off, char val) {
        UNSAFE.putChar(b, BYTE_ARRAY_BASE_OFFSET + off, val);
    }

    @Override
    public void putShort(byte[] b, int off, short val) {
        UNSAFE.putShort(b, BYTE_ARRAY_BASE_OFFSET + off, val);
    }

    @Override
    public void putInt(byte[] b, int off, int val) {
        UNSAFE.putInt(b, BYTE_ARRAY_BASE_OFFSET + off, val);
    }

    @Override
    public void putFloat(byte[] b, int off, float val) {
        UNSAFE.putFloat(b, BYTE_ARRAY_BASE_OFFSET + off, val);
    }

    @Override
    public void putLong(byte[] b, int off, long val) {
        UNSAFE.putLong(b, BYTE_ARRAY_BASE_OFFSET + off, val);
    }

    @Override
    public void putDouble(byte[] b, int off, double val) {
        UNSAFE.putDouble(b, BYTE_ARRAY_BASE_OFFSET + off, val);
    }
}
