package com.indeed.util.unsafe;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class IndeedUnsafe {
    private static final Unsafe UNSAFE;
    public static final long BYTE_ARRAY_BASE_OFFSET;

    static {
        try {
            final Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            UNSAFE = (Unsafe) theUnsafe.get(null);
            BYTE_ARRAY_BASE_OFFSET = UNSAFE.arrayBaseOffset(byte[].class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static long allocateMemory(long bytes) {
        return UNSAFE.allocateMemory(bytes);
    }

    public static long reallocateMemory(long address, long bytes) {
        return UNSAFE.reallocateMemory(address, bytes);
    }

    public static void freeMemory(long address) {
        UNSAFE.freeMemory(address);
    }

    public static byte getByte(long address) {
        return UNSAFE.getByte(address);
    }

    public static char getChar(long address) {
        return UNSAFE.getChar(address);
    }

    public static char getChar(Object o, long offset) {
        return UNSAFE.getChar(o, offset);
    }

    public static short getShort(long address) {
        return UNSAFE.getShort(address);
    }

    public static short getShort(Object o, long offset) {
        return UNSAFE.getShort(o, offset);
    }

    public static int getInt(long address) {
        return UNSAFE.getInt(address);
    }

    public static int getInt(Object o, long offset) {
        return UNSAFE.getInt(o, offset);
    }

    public static float getFloat(long address) {
        return UNSAFE.getFloat(address);
    }

    public static float getFloat(Object o, long offset) {
        return UNSAFE.getFloat(o, offset);
    }

    public static long getLong(long address) {
        return UNSAFE.getLong(address);
    }

    public static long getLong(Object o, long offset) {
        return UNSAFE.getLong(o, offset);
    }

    public static double getDouble(long address) {
        return UNSAFE.getDouble(address);
    }

    public static double getDouble(Object o, long offset) {
        return UNSAFE.getDouble(o, offset);
    }

    public static void putByte(long address, byte x) {
        UNSAFE.putByte(address, x);
    }

    public static void putChar(long address, char x) {
        UNSAFE.putChar(address, x);
    }

    public static void putChar(Object o, long offset, char x) {
        UNSAFE.putChar(o, offset, x);
    }

    public static void putShort(long address, short x) {
        UNSAFE.putShort(address, x);
    }

    public static void putShort(Object o, long offset, short x) {
        UNSAFE.putShort(o, offset, x);
    }

    public static void putInt(long address, int x) {
        UNSAFE.putInt(address, x);
    }

    public static void putInt(Object o, long offset, int x) {
        UNSAFE.putInt(o, offset, x);
    }

    public static void putFloat(long address, float x) {
        UNSAFE.putFloat(address, x);
    }

    public static void putFloat(Object o, long offset, float x) {
        UNSAFE.putFloat(o, offset, x);
    }

    public static void putLong(long address, long x) {
        UNSAFE.putLong(address, x);
    }

    public static void putLong(Object o, long offset, long x) {
        UNSAFE.putLong(o, offset, x);
    }

    public static void putDouble(long address, double x) {
        UNSAFE.putDouble(address, x);
    }

    public static void putDouble(Object o, long offset, double x) {
        UNSAFE.putDouble(o, offset, x);
    }

    public static void copyMemory(long source, long dest, long length) {
        UNSAFE.copyMemory(source, dest, length);
    }
}
