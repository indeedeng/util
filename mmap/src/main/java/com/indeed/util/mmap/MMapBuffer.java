package com.indeed.util.mmap;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.io.Closeables;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.IdentityHashMap;
import java.util.Map;

/** @author jplaisance */
public final class MMapBuffer implements BufferResource {

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

    @VisibleForTesting static Tracker openBuffersTracker;

    static {
        final boolean shouldTrack =
                "true".equals(System.getProperty("com.indeed.util.mmap.MMapBuffer.enableTracking"));
        setTrackingEnabled(shouldTrack);
    }

    private final long address;
    private final DirectMemory memory;

    private static RandomAccessFile open(Path path, FileChannel.MapMode mapMode)
            throws FileNotFoundException {
        if (Files.notExists(path) && mapMode == FileChannel.MapMode.READ_ONLY) {
            throw new FileNotFoundException(path + " does not exist");
        }
        final String openMode;
        if (mapMode == FileChannel.MapMode.READ_ONLY) {
            openMode = "r";
        } else if (mapMode == FileChannel.MapMode.READ_WRITE) {
            openMode = "rw";
        } else {
            throw new IllegalArgumentException(
                    "only MapMode.READ_ONLY and MapMode.READ_WRITE are supported");
        }
        return new RandomAccessFile(path.toFile(), openMode);
    }

    public MMapBuffer(File file, FileChannel.MapMode mapMode, ByteOrder order) throws IOException {
        this(file, 0, file.length(), mapMode, order);
    }

    public MMapBuffer(
            File file, long offset, long length, FileChannel.MapMode mapMode, ByteOrder order)
            throws IOException {
        this(file.toPath(), offset, length, mapMode, order);
    }

    public MMapBuffer(Path path, FileChannel.MapMode mapMode, ByteOrder order) throws IOException {
        this(path, 0, Files.size(path), mapMode, order);
    }

    public MMapBuffer(
            Path path, long offset, long length, FileChannel.MapMode mapMode, ByteOrder order)
            throws IOException {
        this(open(path, mapMode), path, offset, length, mapMode, order, true);
    }

    public MMapBuffer(
            RandomAccessFile raf,
            File file,
            long offset,
            long length,
            FileChannel.MapMode mapMode,
            ByteOrder order)
            throws IOException {
        this(raf, file, offset, length, mapMode, order, false);
    }

    public MMapBuffer(
            RandomAccessFile raf,
            Path path,
            long offset,
            long length,
            FileChannel.MapMode mapMode,
            ByteOrder order)
            throws IOException {
        this(raf, path, offset, length, mapMode, order, false);
    }

    public MMapBuffer(
            RandomAccessFile raf,
            File file,
            long offset,
            long length,
            FileChannel.MapMode mapMode,
            ByteOrder order,
            boolean closeFile)
            throws IOException {
        this(raf, file.toPath(), offset, length, mapMode, order, closeFile);
    }

    public MMapBuffer(
            RandomAccessFile raf,
            Path path,
            long offset,
            long length,
            FileChannel.MapMode mapMode,
            ByteOrder order,
            boolean closeFile)
            throws IOException {
        try {
            if (offset < 0)
                throw new IllegalArgumentException(
                        "error mapping [" + path + "]: offset must be >= 0");
            if (length <= 0) {
                if (length < 0)
                    throw new IllegalArgumentException(
                            "error mapping [" + path + "]: length must be >= 0");
                address = 0;
                memory = new DirectMemory(0, 0, order);
            } else {
                final int prot;
                if (mapMode == FileChannel.MapMode.READ_ONLY) {
                    prot = READ_ONLY;
                } else if (mapMode == FileChannel.MapMode.READ_WRITE) {
                    prot = READ_WRITE;
                } else {
                    throw new IllegalArgumentException(
                            "only MapMode.READ_ONLY and MapMode.READ_WRITE are supported");
                }
                if (raf.length() < offset + length) {
                    if (mapMode == FileChannel.MapMode.READ_WRITE) {
                        raf.setLength(offset + length);
                    } else {
                        throw new IllegalArgumentException(
                                "cannot open file ["
                                        + path
                                        + "] in read only mode with offset+length > file.length()");
                    }
                }
                final int fd;
                try {
                    fd = FD_FIELD.getInt(raf.getFD());
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                address = mmap(length, prot, MAP_SHARED, fd, offset);
                if (address == MAP_FAILED) {
                    final int errno = errno();
                    throw new IOException(
                            "mmap("
                                    + path
                                    + ", "
                                    + offset
                                    + ", "
                                    + length
                                    + ", "
                                    + mapMode
                                    + ") failed [Errno "
                                    + errno
                                    + "]");
                }
                memory = new DirectMemory(address, length, order);
            }
        } finally {
            if (closeFile) {
                Closeables.close(raf, true);
            }
        }

        if (openBuffersTracker != null) {
            openBuffersTracker.mmapBufferOpened(this);
        }
    }

    static native long mmap(long length, int prot, int flags, int fd, long offset);

    static native int munmap(long address, long length);

    static native long mremap(long address, long oldLength, long newLength);

    private static native int msync(long address, long length);

    private static native int madvise(long address, long length);

    private static native int madviseDontNeed(long address, long length);

    static native int errno();

    // this is not particularly useful, the syscall takes forever
    public void advise(long position, long length) throws IOException {
        final long ap = address + position;
        final long a = (ap) / PAGE_SIZE * PAGE_SIZE;
        final long l = Math.min(length + (ap - a), address + memory.length() - ap);
        final int err = madvise(a, l);
        if (err != 0) {
            throw new IOException("madvise failed with error code: " + err);
        }
    }

    public void sync(long position, long length) throws IOException {
        final long ap = address + position;
        final long a = (ap) / PAGE_SIZE * PAGE_SIZE;
        final long l = Math.min(length + (ap - a), address + memory.length() - ap);
        final int err = msync(a, l);
        if (err != 0) {
            throw new IOException("msync failed with error code: " + err);
        }
    }

    public void mlock(long position, long length) {
        if (position < 0) throw new IndexOutOfBoundsException();
        if (length < 0) throw new IndexOutOfBoundsException();
        if (position + length > memory.length()) throw new IndexOutOfBoundsException();
        NativeMemoryUtils.mlock(address + position, length);
    }

    public void munlock(long position, long length) {
        if (position < 0) throw new IndexOutOfBoundsException();
        if (length < 0) throw new IndexOutOfBoundsException();
        if (position + length > memory.length()) throw new IndexOutOfBoundsException();
        NativeMemoryUtils.munlock(address + position, length);
    }

    public void mincore(long position, long length, DirectMemory direct) {
        if (position + length > memory().length()) {
            throw new IndexOutOfBoundsException();
        }
        final long ap = address + position;
        final long a = ap / PAGE_SIZE * PAGE_SIZE;
        final long l = length + (ap - a);
        if ((l + PAGE_SIZE - 1) / PAGE_SIZE > direct.length())
            throw new IndexOutOfBoundsException();
        NativeMemoryUtils.mincore(a, l, direct);
    }

    @VisibleForTesting
    int getErrno() {
        return errno();
    }

    @Override
    public void close() throws IOException {
        if (openBuffersTracker != null) {
            openBuffersTracker.beforeMMapBufferClosed(this);
        }

        // hack to deal with 0 byte files
        if (address != 0) {
            if (munmap(address, memory.length()) != 0)
                throw new IOException("munmap failed [Errno " + errno() + "]");
        }
    }

    public DirectMemory memory() {
        return memory;
    }

    /**
     * @return true, if open buffers tracking is enabled, else false
     *     <p>DO NOT USE THIS unless you know what you're doing.
     */
    @Deprecated
    public static boolean isTrackingEnabled() {
        return openBuffersTracker != null;
    }

    /**
     * If open buffers tracking is enabled, calls madvise with MADV_DONTNEED for all tracked
     * buffers. If open buffers tracking is disabled, does nothing.
     *
     * <p>This can reduce resident set size of the process, but may significantly affect
     * performance. See madvise(2) for more info.
     *
     * <p>DO NOT USE THIS unless you know what you're doing.
     */
    @Deprecated
    public static void madviseDontNeedTrackedBuffers() {
        if (openBuffersTracker == null) {
            return;
        }

        openBuffersTracker.forEachOpenTrackedBuffer(
                new Function<MMapBuffer, Void>() {
                    @Override
                    public Void apply(final MMapBuffer b) {
                        //noinspection deprecation
                        madviseDontNeed(b.memory.getAddress(), b.memory.length());
                        return null;
                    }
                });
    }

    @VisibleForTesting
    static void setTrackingEnabled(final boolean enabled) {
        openBuffersTracker = enabled ? new Tracker() : null;
    }

    @VisibleForTesting
    static class Tracker {
        @VisibleForTesting final Map<MMapBuffer, Void> mmapBufferSet = new IdentityHashMap<>();

        void mmapBufferOpened(final MMapBuffer buffer) {
            synchronized (mmapBufferSet) {
                mmapBufferSet.put(buffer, null);
            }
        }

        void beforeMMapBufferClosed(final MMapBuffer buffer) {
            synchronized (mmapBufferSet) {
                mmapBufferSet.remove(buffer);
            }
        }

        void forEachOpenTrackedBuffer(final Function<MMapBuffer, ?> action) {
            synchronized (mmapBufferSet) {
                for (final MMapBuffer buffer : mmapBufferSet.keySet()) {
                    action.apply(buffer);
                }
            }
        }
    }
}
