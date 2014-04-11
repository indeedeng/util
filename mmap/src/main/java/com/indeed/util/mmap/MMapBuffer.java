package com.indeed.util.mmap;

import com.google.common.annotations.VisibleForTesting;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

/**
 * @author jplaisance
 */
public final class MMapBuffer implements BufferResource {

    private static final Logger log = Logger.getLogger(MMapBuffer.class);

    private static final Field FD_FIELD;
    public static final int PAGE_SIZE = 4096;

    private static final int READ_ONLY = 0;
    static final int READ_WRITE = 1;

    static final long MAP_FAILED = -1L;

    static final int MAP_SHARED = 1;
    static final int MAP_PRIVATE = 2;
    static final int MAP_ANONYMOUS = 4;

    static {
        LoadIndeedMMap.loadLibrary();
        try {
            FD_FIELD = FileDescriptor.class.getDeclaredField("fd");
            FD_FIELD.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private final long address;
    private final DirectMemory memory;

    public MMapBuffer(File file, FileChannel.MapMode mapMode, ByteOrder order) throws IOException {
        this(file, 0, file.length(), mapMode, order);
    }

    public MMapBuffer(File file, long offset, long length, FileChannel.MapMode mapMode, ByteOrder order) throws IOException {
        if (!file.exists() && mapMode == FileChannel.MapMode.READ_ONLY) {
            throw new FileNotFoundException(file + " does not exist");
        }

        if (offset < 0) throw new IllegalArgumentException("error mapping [" + file + "]: offset must be >= 0");
        if (length <= 0) {
            if (length < 0) throw new IllegalArgumentException("error mapping [" + file + "]: length must be >= 0");
            address = 0;
            memory = new DirectMemory(0, 0, order);
        } else {
            final String openMode;
            int prot;
            if (mapMode == FileChannel.MapMode.READ_ONLY) {
                openMode = "r";
                prot = READ_ONLY;
            } else if (mapMode == FileChannel.MapMode.READ_WRITE) {
                openMode = "rw";
                prot = READ_WRITE;
            } else {
                throw new IllegalArgumentException("only MapMode.READ_ONLY and MapMode.READ_WRITE are supported");
            }
            RandomAccessFile raf = new RandomAccessFile(file, openMode);
            if (raf.length() < offset+length) {
                if (mapMode == FileChannel.MapMode.READ_WRITE) {
                    raf.setLength(offset+length);
                    raf.close();
                    raf = new RandomAccessFile(file, openMode);
                } else {
                    throw new IllegalArgumentException("cannot open file [" + file + "] in read only mode with offset+length > file.length()");
                }
            }
            int fd;
            try {
                fd = FD_FIELD.getInt(raf.getFD());
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            address = mmap(length, prot, MAP_SHARED, fd, offset);
            try {
                raf.close();
            } catch (IOException e) {
                //ignore
            }
            if (address == MAP_FAILED) {
                int errno = errno();
                throw new IOException("mmap(" + file.getAbsolutePath() + ", " + offset + ", " + length + ", " + mapMode + ") failed [Errno " + errno + "]");
            }
            memory = new DirectMemory(address, length, order);
        }
    }

    static native long mmap(long length, int prot, int flags, int fd, long offset);

    static native int munmap(long address, long length);

    static native long mremap(long address, long oldLength, long newLength);

    private static native int msync(long address, long length);

    private static native int madvise(long address, long length);

    static native int errno();

    //this is not particularly useful, the syscall takes forever
    public void advise(long position, long length) throws IOException {
        final long ap = address+position;
        final long a = (ap)/PAGE_SIZE*PAGE_SIZE;
        final long l = Math.min(length+(ap-a), address+memory.length()-ap);
        final int err = madvise(a, l);
        if (err != 0) {
            throw new IOException("madvise failed with error code: "+err);
        }
    }

    public void sync(long position, long length) throws IOException {
        final long ap = address+position;
        final long a = (ap)/PAGE_SIZE*PAGE_SIZE;
        final long l = Math.min(length+(ap-a), address+memory.length()-ap);
        final int err = msync(a, l);
        if (err != 0) {
            throw new IOException("msync failed with error code: "+err);
        }
    }

    public void mlock(long position, long length) {
        if (position < 0) throw new IndexOutOfBoundsException();
        if (length < 0) throw new IndexOutOfBoundsException();
        if (position+length > memory.length()) throw new IndexOutOfBoundsException();
        NativeMemoryUtils.mlock(address+position, length);
    }

    public void munlock(long position, long length) {
        if (position < 0) throw new IndexOutOfBoundsException();
        if (length < 0) throw new IndexOutOfBoundsException();
        if (position+length > memory.length()) throw new IndexOutOfBoundsException();
        NativeMemoryUtils.munlock(address+position, length);
    }

    public void mincore(long position, long length, DirectMemory direct) {
        if (position+length > memory().length()) {
            throw new IndexOutOfBoundsException();
        }
        final long ap = address+position;
        final long a = ap/PAGE_SIZE*PAGE_SIZE;
        final long l = length+(ap-a);
        if ((l+PAGE_SIZE-1)/PAGE_SIZE > direct.length()) throw new IndexOutOfBoundsException();
        NativeMemoryUtils.mincore(a, l, direct);
    }

    @VisibleForTesting
    int getErrno() {
        return errno();
    }

    @Override
    public void close() throws IOException {
        //hack to deal with 0 byte files
        if (address != 0) {
            if (munmap(address, memory.length()) != 0) throw new IOException("munmap failed [Errno " + errno() + "]");
        }
    }
    
    public DirectMemory memory() {
        return memory;
    }
}
