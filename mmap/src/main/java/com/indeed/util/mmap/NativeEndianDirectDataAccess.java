package com.indeed.util.mmap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

/** @author jplaisance */
final class NativeEndianDirectDataAccess implements DirectDataAccess {
    private static final Logger log = LoggerFactory.getLogger(NativeEndianDirectDataAccess.class);

    private static final Unsafe UNSAFE;
    private static final NativeEndianDirectDataAccess instance = new NativeEndianDirectDataAccess();

    public static NativeEndianDirectDataAccess getInstance() {
        return instance;
    }

    static {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            UNSAFE = (Unsafe) theUnsafe.get(null);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private NativeEndianDirectDataAccess() {}

    @Override
    public byte getByte(long address) {
        return UNSAFE.getByte(address);
    }

    @Override
    public char getChar(long address) {
        return UNSAFE.getChar(address);
    }

    @Override
    public short getShort(long address) {
        return UNSAFE.getShort(address);
    }

    @Override
    public int getInt(long address) {
        return UNSAFE.getInt(address);
    }

    @Override
    public float getFloat(long address) {
        return UNSAFE.getFloat(address);
    }

    @Override
    public long getLong(long address) {
        return UNSAFE.getLong(address);
    }

    @Override
    public double getDouble(long address) {
        return UNSAFE.getDouble(address);
    }

    @Override
    public void putByte(long address, byte val) {
        UNSAFE.putByte(address, val);
    }

    @Override
    public void putChar(long address, char val) {
        UNSAFE.putChar(address, val);
    }

    @Override
    public void putShort(long address, short val) {
        UNSAFE.putShort(address, val);
    }

    @Override
    public void putInt(long address, int val) {
        UNSAFE.putInt(address, val);
    }

    @Override
    public void putFloat(long address, float val) {
        UNSAFE.putFloat(address, val);
    }

    @Override
    public void putLong(long address, long val) {
        UNSAFE.putLong(address, val);
    }

    @Override
    public void putDouble(long address, double val) {
        UNSAFE.putDouble(address, val);
    }

    @Override
    public void copyMemory(long source, long dest, long length) {
        UNSAFE.copyMemory(source, dest, length);
    }
}
