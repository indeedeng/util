package com.indeed.util.mmap;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteOrder;

/**
 * @author jplaisance
 */
public final class Stat {

    private static final Logger log = LogManager.getLogger(Stat.class);

    private static final Field fdField;

    //st_mode flags
    private static final int S_IFMT  = 0xf000;
    private static final int S_IFSOCK = 0xc000;
    private static final int S_IFLNK = 0xa000;
    private static final int S_IFREG = 0x8000;
    private static final int S_IFBLK = 0x6000;
    private static final int S_IFDIR = 0x4000;
    private static final int S_IFCHR = 0x2000;
    private static final int S_IFIFO = 0x1000;
    private static final int S_ISUID = 0x800;
    private static final int S_ISGID = 0x400;
    private static final int S_ISVTX = 0x200;
    private static final int S_IRWXU = 0x1c0;
    private static final int S_IRUSR = 0x100;
    private static final int S_IWUSR = 0x80;
    private static final int S_IXUSR = 0x40;
    private static final int S_IRWXG = 0x38;
    private static final int S_IRGRP = 0x20;
    private static final int S_IWGRP = 0x10;
    private static final int S_IXGRP = 0x8;
    private static final int S_IRWXO = 0x7;
    private static final int S_IROTH = 0x4;
    private static final int S_IWOTH = 0x2;
    private static final int S_IXOTH = 0x1;

    private static final ImmutableList<String> errstr = ImmutableList.of("OK", "EACCES", "EBADF", "EFAULT", "ELOOP", "ENAMETOOLONG", "ENOENT", "ENOMEM", "ENOTDIR", "EOVERFLOW");

    static {
        try {
            fdField = FileDescriptor.class.getDeclaredField("fd");
            fdField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw Throwables.propagate(e);
        }
    }

    static {
        LoadIndeedMMap.loadLibrary();
    }

    public static Stat stat(String file) throws IOException {
        NativeBuffer nativeBuffer = new NativeBuffer(92, ByteOrder.nativeOrder());
        try {
            DirectMemory direct = nativeBuffer.memory();
            int err = stat(file, direct.getAddress());
            if (err != 0) {
                if (err == errstr.indexOf("ENOENT")) {
                    throw new FileNotFoundException("No such file or directory: "+file);
                }
                throw new IOException("stat on path "+file+" failed with error "+(err < 0 ? "unknown" : errstr.get(err)));
            }
            return new Stat(direct);
        } finally {
            nativeBuffer.close();
        }
    }

    public static Stat stat(File file) throws IOException {
        return stat(file.getPath());
    }

    public static Stat lstat(String file) throws IOException {
        NativeBuffer nativeBuffer = new NativeBuffer(92, ByteOrder.nativeOrder());
        try {
            DirectMemory direct = nativeBuffer.memory();
            int err = lstat(file, direct.getAddress());
            if (err == errstr.indexOf("ENOENT")) {
                throw new FileNotFoundException("No such file or directory: "+file);
            }
            if (err != 0) {
                throw new IOException("stat on path "+file+" failed with error "+(err < 0 ? "unknown" : errstr.get(err)));
            }
            return new Stat(direct);
        } finally {
            nativeBuffer.close();
        }
    }

    public static Stat lstat(File file) throws IOException {
        return lstat(file.getPath());
    }

    public static Stat fstat(FileDescriptor fd) throws IOException {
        NativeBuffer nativeBuffer = new NativeBuffer(92, ByteOrder.nativeOrder());
        try {
            DirectMemory direct = nativeBuffer.memory();
            try {
                int err = fstat(fdField.getInt(fd), direct.getAddress());
                if (err == errstr.indexOf("ENOENT")) {
                    throw new FileNotFoundException("No such file or directory");
                }
                if (err != 0) {
                    throw new IOException("fstat on file descriptor "+fd+" failed with error "+(err < 0 ? "unknown" : errstr.get(err)));
                }
            } catch (IllegalAccessException e) {
                throw Throwables.propagate(e);
            }
            return new Stat(direct);
        } finally {
            nativeBuffer.close();
        }
    }

    private static native int stat(String path, long addr);
    private static native int lstat(String path, long addr);
    private static native int fstat(int fd, long addr);

    private final long device;
    private final long inode;
    private final int mode;
    private final long numLinks;
    private final int uid;
    private final int gid;
    private final long rdev;
    private final long size;
    private final long blockSize;
    private final long numBlocks;
    private final long aTime;
    private final long mTime;
    private final long cTime;

    /**
     * st_dev: 8
     * st_ino: 8
     * st_mode: 4
     * st_nlink: 8
     * st_uid: 4
     * st_gid: 4
     * st_rdev: 8
     * st_size: 8
     * st_blksize: 8
     * st_blocks: 8
     * st_atime: 8
     * st_mtime: 8
     * st_ctime: 8
     */
    private Stat(DirectMemory direct) {
        device = direct.getLong(0);
        inode = direct.getLong(8);
        mode = direct.getInt(16);
        numLinks = direct.getLong(20);
        uid = direct.getInt(28);
        gid = direct.getInt(32);
        rdev = direct.getLong(36);
        size = direct.getLong(44);
        blockSize = direct.getLong(52);
        numBlocks = direct.getLong(60);
        aTime = direct.getLong(68);
        mTime = direct.getLong(76);
        cTime = direct.getLong(84);
    }

    public long getDevice() {
        return device;
    }

    public long getInode() {
        return inode;
    }

    public int getMode() {
        return mode;
    }

    public long getNumLinks() {
        return numLinks;
    }

    public int getUid() {
        return uid;
    }

    public int getGid() {
        return gid;
    }

    public long getRdev() {
        return rdev;
    }

    public long getSize() {
        return size;
    }

    public long getBlockSize() {
        return blockSize;
    }

    public long getNumBlocks() {
        return numBlocks;
    }

    public long getaTime() {
        return aTime;
    }

    public long getmTime() {
        return mTime;
    }

    public long getcTime() {
        return cTime;
    }

    private int getFileTypeBits() {
        return getMode() & S_IFMT;
    }

    public boolean isRegularFile() {
        return getFileTypeBits() == S_IFREG;
    }

    public boolean isDirectory() {
        return getFileTypeBits() == S_IFDIR;
    }

    public boolean isCharacterDevice() {
        return getFileTypeBits() == S_IFCHR;
    }

    public boolean isBlockDevice() {
        return getFileTypeBits() == S_IFBLK;
    }

    public boolean isFifo() {
        return getFileTypeBits() == S_IFIFO;
    }

    public boolean isSymlink() {
        return getFileTypeBits() == S_IFLNK;
    }

    public boolean isSock() {
        return getFileTypeBits() == S_IFSOCK;
    }

    public boolean getSUID() {
        return (getMode() & S_ISUID) == S_ISUID;
    }

    public boolean getSGID() {
        return (getMode() & S_ISGID) == S_ISGID;
    }

    public boolean isSticky() {
        return (getMode() & S_ISVTX) == S_ISVTX;
    }

    public boolean isUserReadable() {
        return (getMode() & S_IRUSR) == S_IRUSR;
    }

    public boolean isUserWritable() {
        return (getMode() & S_IWUSR) == S_IWUSR;
    }

    public boolean isUserExecutable() {
        return (getMode() & S_IXUSR) == S_IXUSR;
    }

    public boolean isGroupReadable() {
        return (getMode() & S_IRGRP) == S_IRGRP;
    }

    public boolean isGroupWritable() {
        return (getMode() & S_IWGRP) == S_IWGRP;
    }

    public boolean isGroupExecutable() {
        return (getMode() & S_IXGRP) == S_IXGRP;
    }

    public boolean isOtherReadable() {
        return (getMode() & S_IROTH) == S_IROTH;
    }

    public boolean isOtherWritable() {
        return (getMode() & S_IWOTH) == S_IWOTH;
    }

    public boolean isOtherExecutable() {
        return (getMode() & S_IXOTH) == S_IXOTH;
    }

    @Override
    public String toString() {
        return "Stat{" +
                "device=" +
                device +
                ", inode=" +
                inode +
                ", mode=" +
                String.format("%x", mode) +
                ", numLinks=" +
                numLinks +
                ", uid=" +
                uid +
                ", gid=" +
                gid +
                ", rdev=" +
                rdev +
                ", size=" +
                size +
                ", blockSize=" +
                blockSize +
                ", numBlocks=" +
                numBlocks +
                ", aTime=" +
                aTime +
                ", mTime=" +
                mTime +
                ", cTime=" +
                cTime +
                '}';
    }
}
