package com.indeed.util.mmap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

/**
 * @author jplaisance
 */
final class NativeMemoryUtils {
    private static final Logger log = LoggerFactory.getLogger(NativeMemoryUtils.class);

    static {
        LoadIndeedMMap.loadLibrary();
    }

    //copied from java.nio.Bits
    private static final long UNSAFE_COPY_THRESHOLD = 1024L * 1024L;

    private static final int ENOMEM = 1;

    private static final int EPERM = 2;

    private static final int EAGAIN = 3;

    private static final int EFAULT = 4;

    private static final int EINVAL = 5;

    static void copyFromArray(byte[] src, int offset, long dstAddr, int length) {
        while (length > 0) {
            long size = (length > UNSAFE_COPY_THRESHOLD) ? UNSAFE_COPY_THRESHOLD : length;
            copyFromByteArray(src, offset, (int)size, dstAddr);
            length -= size;
            offset += size;
            dstAddr += size;
        }
    }

    static void copyToArray(long srcAddr, byte[] dst, int offset, int length) {
        while (length > 0) {
            long size = (length > UNSAFE_COPY_THRESHOLD) ? UNSAFE_COPY_THRESHOLD : length;
            copyToByteArray(srcAddr, (int)size, dst, offset);
            length -= size;
            srcAddr += size;
            offset += size;
        }
    }

    static void mlock(long addr, long len) {
        if (len < 0) throw new IndexOutOfBoundsException();
        int err = mlock0(addr, len);
        if (err != 0) {
            if (err == EAGAIN) {
                mlock(addr, len);
                return;
            }
            throw new OutOfMemoryError();
        }
    }

    static void munlock(long addr, long len) {
        if (len < 0) throw new IndexOutOfBoundsException();
        int err = munlock0(addr, len);
        if (err != 0) {
            if (err == EAGAIN) {
                munlock(addr, len);
                return;
            }
            throw new OutOfMemoryError();
        }
    }

    static void mincore(long addr, long length, DirectMemory memory) {
        int err = mincore(addr, length, memory.getAddress());
        if (err != 0) {
            switch (err) {
                case ENOMEM: throw new IllegalArgumentException("illegal arguments: address: "+addr+" length: "+length+" vec: "+memory.getAddress());
                case EAGAIN:
                    mincore(addr, length, memory);
                    return;
                case EFAULT: throw new IllegalArgumentException("memory at "+memory.getAddress()+" is not valid");
                case EINVAL: throw new IllegalArgumentException("address "+addr+" is not a multiple of the page size");
                default: throw new IllegalArgumentException("unknown error");
            }
        }
    }

    static native void copyToDirectBuffer(long srcAddr, ByteBuffer dest, int offset, int length);

    static native void copyFromDirectBuffer(ByteBuffer source, int offset, long destAddr, int length);

    private static native void copyFromByteArray(byte[] src, int offset, int length, long addr);

    private static native void copyToByteArray(long addr, int length, byte[] dest, int offset);

    private static native int mlock0(long addr, long len);

    private static native int munlock0(long addr, long len);

    private static native int mincore(long addr, long length, long vec);
}
