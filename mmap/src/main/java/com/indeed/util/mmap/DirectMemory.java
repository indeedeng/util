package com.indeed.util.mmap;

import org.apache.log4j.Logger;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author jplaisance
 */
public final class DirectMemory extends AbstractMemory {
    private static final Logger log = Logger.getLogger(DirectMemory.class);
    private static final boolean debug = true;

    private final ByteOrder order;
    private final DirectDataAccess directDataAccess;

    private final long address;
    private final long length;

    DirectMemory(long address, long length, ByteOrder order) {
        this.address = address;
        this.length = length;
        this.order = order;
        directDataAccess = order.equals(ByteOrder.nativeOrder()) ? NativeEndianDirectDataAccess.getInstance() : ReverseEndianDirectDataAccess.getInstance();
    }

    private void checkBounds(long l, long length) {
        if (debug) {
            if (l < 0 || l > this.length-length) throw new IndexOutOfBoundsException("l: "+l+" length: "+length);
        }
    }

    private void checkArrayBounds(final byte[] bytes, final int start, final int length) {
        if (debug) {
            if (bytes == null) throw new IllegalArgumentException("byte[] bytes cannot be null");
            if (start < 0) throw new IndexOutOfBoundsException("start cannot be less than zero");
            if (length < 0) throw new IllegalArgumentException("length cannot be less than zero");
            if (start+length > bytes.length) throw new IndexOutOfBoundsException("start plus length cannot be greater than length of byte[] bytes");
            if (start+length < 0) throw new IndexOutOfBoundsException("start plus length cannot be greater than Integer.MAX_VALUE");
        }
    }

    @Override
    public byte getByte(final long l) {
        checkBounds(l, 1);
        return directDataAccess.getByte(address+l);
    }

    @Override
    public void putByte(final long l, final byte b) {
        checkBounds(l, 1);
        directDataAccess.putByte(address + l, b);
    }

    @Override
    public short getShort(final long l) {
        checkBounds(l, 2);
        return directDataAccess.getShort(address+l);
    }

    @Override
    public void putShort(final long l, final short i) {
        checkBounds(l, 2);
        directDataAccess.putShort(address + l, i);
    }

    @Override
    public char getChar(final long l) {
        checkBounds(l, 2);
        return directDataAccess.getChar(address+l);
    }

    @Override
    public void putChar(final long l, final char c) {
        checkBounds(l, 2);
        directDataAccess.putChar(address + l, c);
    }

    @Override
    public int getInt(final long l) {
        checkBounds(l, 4);
        return directDataAccess.getInt(address+l);
    }

    @Override
    public void putInt(final long l, final int i) {
        checkBounds(l, 4);
        directDataAccess.putInt(address + l, i);
    }

    @Override
    public long getLong(final long l) {
        checkBounds(l, 8);
        return directDataAccess.getLong(address+l);
    }

    @Override
    public void putLong(final long l, final long l1) {
        checkBounds(l, 8);
        directDataAccess.putLong(address + l, l1);
    }

    @Override
    public float getFloat(final long l) {
        checkBounds(l, 4);
        return directDataAccess.getFloat(address+l);
    }

    @Override
    public void putFloat(final long l, final float v) {
        checkBounds(l, 4);
        directDataAccess.putFloat(address + l, v);
    }

    @Override
    public double getDouble(final long l) {
        checkBounds(l, 8);
        return directDataAccess.getDouble(address+l);
    }

    @Override
    public void putDouble(final long l, final double v) {
        checkBounds(l, 8);
        directDataAccess.putDouble(address + l, v);
    }

    @Override
    public void putBytes(final long l, final byte[] bytes) {
        putBytes(l, bytes, 0, bytes.length);
    }

    @Override
    public void putBytes(final long l, final byte[] bytes, final int start, final int length) {
        checkBounds(l, length);
        checkArrayBounds(bytes, start, length);
        NativeMemoryUtils.copyFromArray(bytes, start, address + l, length);
    }

    public void putBytes(long l, DirectMemory source) {
        putBytes(l, source, 0, source.length());
    }

    public void putBytes(long l, DirectMemory source, long start, long length) {
        checkBounds(l, length);
        source.checkBounds(start, length);
        directDataAccess.copyMemory(source.address + start, address + l, length);
    }

    @Override
    public void putBytes(long l, Memory source) {
        putBytes(l, source, 0, source.length());
    }

    @Override
    public void putBytes(long l, Memory source, long start, long length) {
        if (source.isDirect()) {
            putBytes(l, (DirectMemory) source, start, length);
        } else {
            source.getBytes(start, this, l, length);
        }
    }

    @Override
    public void putBytes(long l, ByteBuffer source) {
        final int length = source.remaining();
        checkBounds(l, length);
        if (source.isDirect()) {
            NativeMemoryUtils.copyFromDirectBuffer(source, source.position(), address+l, length);
            source.position(source.position()+length);
        } else if (source.hasArray()) {
            final byte[] array = source.array();
            final int offset = source.arrayOffset();
            putBytes(l, array, offset+source.position(), length);
            source.position(source.position()+length);
        } else {
            final byte[] copyBuffer = new byte[Math.min(length, 4096)];
            long destAddr = l;
            while (source.remaining() > 0) {
                final int copySize = Math.min(copyBuffer.length, source.remaining());
                source.get(copyBuffer, 0, copySize);
                putBytes(destAddr, copyBuffer, 0, copySize);
                destAddr+=copySize;
            }
        }
    }

    @Override
    public void getBytes(final long l, final byte[] bytes) {
        getBytes(l, bytes, 0, bytes.length);
    }

    @Override
    public void getBytes(final long l, final byte[] bytes, final int start, final int length) {
        checkBounds(l, length);
        checkArrayBounds(bytes, start, length);
        NativeMemoryUtils.copyToArray(address + l, bytes, start, length);
    }

    public void getBytes(long l, DirectMemory dest) {
        getBytes(l, dest, 0, dest.length());
    }

    public void getBytes(long l, DirectMemory dest, long start, long length) {
        checkBounds(l, length);
        dest.checkBounds(start, length);
        directDataAccess.copyMemory(address + l, dest.address + start, length);
    }

    @Override
    public void getBytes(long l, Memory dest) {
        getBytes(l, dest, 0, dest.length());
    }

    @Override
    public void getBytes(long l, Memory dest, long start, long length) {
        if (dest.isDirect()) {
            getBytes(l, (DirectMemory) dest, start, length);
        } else {
            dest.putBytes(start, this, l, length);
        }
    }

    @Override
    public void getBytes(long l, ByteBuffer dest) {
        final int length = dest.remaining();
        checkBounds(l, length);
        if (dest.isDirect()) {
            NativeMemoryUtils.copyToDirectBuffer(address+l, dest, dest.position(), length);
            dest.position(dest.position()+length);
        } else if (dest.hasArray()) {
            final byte[] array = dest.array();
            final int offset = dest.arrayOffset();
            getBytes(l, array, offset+dest.position(), length);
            dest.position(dest.position()+length);
        } else {
            final byte[] copyBuffer = new byte[Math.min(length, 4096)];
            long sourceAddr = l;
            while (dest.remaining() > 0) {
                final int copySize = Math.min(copyBuffer.length, dest.remaining());
                getBytes(sourceAddr, copyBuffer, 0, copySize);
                dest.put(copyBuffer, 0, copySize);
                sourceAddr+=copySize;
            }
        }
    }

    @Override
    public DirectMemory slice(long startAddress, long sliceLength) {
        if (startAddress < 0) throw new IllegalArgumentException("startAddress must be >= 0");
        if (sliceLength < 0) throw new IllegalArgumentException("sliceLength must be >= 0");
        if (startAddress+sliceLength > length) throw new IllegalArgumentException("startAddress+sliceLength must be <= length");
        return new DirectMemory(address+startAddress, sliceLength, order);
    }

    @Override
    public long length() {
        return length;
    }

    @Override
    public boolean isDirect() {
        return true;
    }

    @Override
    public ByteOrder getOrder() {
        return order;
    }

    /**
     * this is really only here for passing to jni calls. don't use it for anything else. deprecated to make your code ugly if you use it.
     * @return address
     */
    @Deprecated
    public long getAddress() {
        return address;
    }
}
