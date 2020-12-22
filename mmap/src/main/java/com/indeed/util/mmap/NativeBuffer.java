package com.indeed.util.mmap;

import com.google.common.base.Throwables;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import sun.misc.Unsafe;

import java.io.Closeable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.nio.ByteOrder;

/**
 * @author jplaisance
 */
public final class NativeBuffer implements BufferResource {
    private static final Logger log = LogManager.getLogger(NativeBuffer.class);

    private static final Unsafe UNSAFE;
    private static final Field FD_FIELD;

    private static final long MMAP_THRESHOLD;

    private static final boolean MAP_ANONYMOUS_DEV_ZERO;

    private static final boolean OS_TYPE_IS_MAC;

    static {
        MAP_ANONYMOUS_DEV_ZERO = Boolean.getBoolean("indeed.mmap.map.anonymous.dev.zero");
        final String thresholdString = System.getProperty("indeed.mmap.threshold");
        long mmapThreshold;
        if (thresholdString == null) {
            mmapThreshold = 256*1024;
        } else {
            try {
                mmapThreshold = Integer.parseInt(thresholdString);
            } catch (NumberFormatException e) {
                log.error("error setting MMAP_THRESHOLD", e);
                mmapThreshold = 256*1024;
            }
        }
        MMAP_THRESHOLD = mmapThreshold;
        try {
            final Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            UNSAFE = (Unsafe)theUnsafe.get(null);
            FD_FIELD = FileDescriptor.class.getDeclaredField("fd");
            FD_FIELD.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        OS_TYPE_IS_MAC = System.getProperty("os.name").toLowerCase().indexOf("mac") >= 0;
    }

    static {
        LoadIndeedMMap.loadLibrary();
    }

    private final long address;
    private final DirectMemory memory;
    private boolean closed = false;
    private final boolean mmapped;

    public NativeBuffer(long length, ByteOrder order) {
        if (length <= 0) {
            if (length < 0)
            throw new IllegalArgumentException("length must be >= 0");
            address = 0;
            memory = new DirectMemory(0, 0, order);
            mmapped = false;
        } else if (length >= MMAP_THRESHOLD) {
            mmapped = true;
            if (MAP_ANONYMOUS_DEV_ZERO) {
                final File devZero = new File("/dev/zero");
                final RandomAccessFile raf;
                try {
                    raf = new RandomAccessFile(devZero, "rw");
                } catch (FileNotFoundException e) {
                    throw Throwables.propagate(e);
                }
                try {
                    address = MMapBuffer.mmap(length, MMapBuffer.READ_WRITE, MMapBuffer.MAP_PRIVATE, FD_FIELD.getInt(raf.getFD()), 0);
                    if (address == MMapBuffer.MAP_FAILED) {
                        throw new RuntimeException("mmap /dev/zero failed with error "+MMapBuffer.errno());
                    }
                    memory = new DirectMemory(address, length, order);
                } catch (IOException e) {
                    throw Throwables.propagate(e);
                } catch (IllegalAccessException e) {
                    throw Throwables.propagate(e);
                } finally {
                    closeQuietly(raf);
                }
            } else {
                address = MMapBuffer.mmap(length, MMapBuffer.READ_WRITE, MMapBuffer.MAP_PRIVATE | MMapBuffer.MAP_ANONYMOUS, -1, 0);
                if (address == MMapBuffer.MAP_FAILED) {
                    throw new RuntimeException("anonymous mmap failed with error "+MMapBuffer.errno());
                }
                memory = new DirectMemory(address, length, order);
            }
        } else {
            address = UNSAFE.allocateMemory(length);
            if (address == 0) throw new OutOfMemoryError();
            memory = new DirectMemory(address, length, order);
            mmapped = false;
        }
    }

    private NativeBuffer(long address, DirectMemory memory, boolean mmapped) {
        this.address = address;
        this.memory = memory;
        this.mmapped = mmapped;
    }

    private NativeBuffer realloc0(long newLength) {
        if (mmapped) throw new UnsupportedOperationException();
        if (newLength >= MMAP_THRESHOLD) throw new UnsupportedOperationException();
        if (newLength <= 0) throw new IllegalArgumentException("length must be > 0");
        final long newAddress = UNSAFE.reallocateMemory(address, newLength);
        if (address == 0) throw new OutOfMemoryError();
        closed = true;
        return new NativeBuffer(newAddress, new DirectMemory(newAddress, newLength, memory.getOrder()), false);
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

    private NativeBuffer createNewAndClose(long newSize) {
        final NativeBuffer ret = new NativeBuffer(newSize, memory.getOrder());
        ret.memory().putBytes(0, memory, 0, Math.min(memory.length(), newSize));
        closeQuietly(this);
        return ret;
    }

    public NativeBuffer realloc(long newSize) {
        if (mmapped && newSize >= MMAP_THRESHOLD) {
            if (OS_TYPE_IS_MAC) {
                // MAC does not support mremap
                return createNewAndClose(newSize);
            }
            else {
                final long newAddress = MMapBuffer.mremap(address, memory.length(), newSize);
                if (newAddress == MMapBuffer.MAP_FAILED) {
                    throw new RuntimeException("anonymous mremap failed with error " + MMapBuffer.errno());
                }
                return new NativeBuffer(newAddress, new DirectMemory(newAddress, newSize, memory.getOrder()), true);
            }
        } else if (!mmapped && newSize < MMAP_THRESHOLD) {
            return realloc0(newSize);
        } else {
            return createNewAndClose(newSize);
        }
    }

    @Override
    public void close() throws IOException {
        if (!closed) {
            closed = true;
            if (mmapped) {
                MMapBuffer.munmap(address, memory.length());
            } else {
                if (address != 0) UNSAFE.freeMemory(address);
            }
        }
    }
    
    public DirectMemory memory() {
        return memory;
    }

    private static void closeQuietly(final Closeable closeable) {
        try {
            if (null != closeable) {
                closeable.close();
            }
        } catch (Exception e) {
            log.error("Exception during cleanup of a Closeable, ignoring", e);
        }
    }
}
