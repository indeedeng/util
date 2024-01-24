// Copyright 2015 Indeed
package com.indeed.util.io;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.indeed.util.core.io.Closeables2;
import com.indeed.util.core.nullsafety.ParametersAreNonnullByDefault;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermissions;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utilities for atomic (and fsync-friendly) operations on files.
 *
 * <p>When possible methods on this class should be used over the ones in {@link
 * com.indeed.util.io.Files}
 *
 * @author rboyer
 */
@ParametersAreNonnullByDefault
public final class SafeFiles {
    private static final Logger LOG = LoggerFactory.getLogger(SafeFiles.class);

    /**
     * Perform an atomic rename of oldName -&gt; newName and fsync the containing directory. This is
     * only truly fsync-safe if both files are in the same directory, but it will at least try to do
     * the right thing if the files are in different directories.
     *
     * @param oldName original file
     * @param newName new file
     * @throws IOException In the event that we could not rename the path.
     */
    public static void rename(final Path oldName, final Path newName) throws IOException {
        checkNotNull(oldName);
        checkNotNull(newName);

        final boolean sameDir = Files.isSameFile(oldName.getParent(), newName.getParent());

        // rename the file
        Files.move(oldName, newName, StandardCopyOption.ATOMIC_MOVE);

        // fsync the parent dir
        fsync(newName.getParent());

        if (!sameDir) {
            fsync(oldName.getParent());
        }
    }

    /**
     * Create a directory if it does not already exist. Fails if the path exists and is NOT a
     * directory. Will fsync the parent directory inode.
     *
     * @param path path to ensure is a directory.
     * @throws IOException In the event that the path is not a directory.
     */
    public static void ensureDirectoryExists(final Path path) throws IOException {
        if (Files.exists(path)) {
            if (!Files.isDirectory(path)) {
                throw new IOException("path is not a directory: " + path);
            }
            // probably should fsync parent here just to be sure, but that might slow stuff down
        } else {
            Files.createDirectories(path);
            fsyncLineage(path.getParent());
        }
    }

    /**
     * Walk a directory tree and Fsync both Directory and File inodes. This does NOT follow symlinks
     * and does not attempt to fsync anything other than Directory or NormalFiles.
     *
     * @param root directory to start the traversal.
     * @return number of NormalFiles fsynced (not including directories).
     * @throws IOException in the event that we could not fsync the provided directory.
     */
    public static int fsyncRecursive(final Path root) throws IOException {
        final FsyncingSimpleFileVisitor visitor = new FsyncingSimpleFileVisitor();
        Files.walkFileTree(root, visitor);
        return visitor.getFileCount();
    }

    private static class FsyncingSimpleFileVisitor extends SimpleFileVisitor<Path> {
        private int fileCount = 0;

        public int getFileCount() {
            return fileCount;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (attrs.isRegularFile()) { // no symlinks, pipes, or device nodes please
                fsync(file);
                fileCount++;
            }

            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                throws IOException {
            if (attrs.isDirectory()) { // safety
                fsync(dir);
            }

            return FileVisitResult.CONTINUE;
        }
    }

    /**
     * Fsync a single path. Please only call this on things that are Directories or NormalFiles.
     *
     * @param path path to fsync
     * @throws IOException in the event that we could not fsync the provided path.
     */
    public static void fsync(final Path path) throws IOException {
        if (!Files.isDirectory(path) && !Files.isRegularFile(path)) {
            throw new IllegalArgumentException(
                    "fsync is only supported for regular files and directories: " + path);
        }

        try (final FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            channel.force(true);
        }
    }

    /**
     * Fsync a path and all parents all the way up to the fs root.
     *
     * @param path The path that we want to fsync.
     */
    private static void fsyncLineage(final Path path) throws IOException {
        Path cursor = path.toRealPath();
        while (cursor != null) {
            fsync(cursor);
            cursor = cursor.getParent();
        }
    }

    /**
     * Write the string to a temporary file, fsync the file, then atomically rename to the target
     * path. On error it will make a best-effort to erase the temporary file.
     *
     * @param value string value to write to file as UTF8 bytes
     * @param path path to write out to
     * @throws IOException in the event that the data could not be written to the path.
     */
    public static void writeUTF8(final String value, final Path path) throws IOException {
        write(value.getBytes(Charsets.UTF_8), path);
    }

    /**
     * Write the bytes to a temporary file, fsync the file, then atomically rename to the target
     * path. On error it will make a best-effort to erase the temporary file.
     *
     * @param data binary value to write to file
     * @param path path to write out to
     * @throws IOException in the event that the data could not be written to the path.
     */
    public static void write(final byte[] data, final Path path) throws IOException {
        try (final SafeOutputStream out = createAtomicFile(path)) {
            out.write(ByteBuffer.wrap(data));
            out.commit();
        }
    }

    /**
     * This is just like a lazy variation of {@link SafeFiles#write}. It opens a temp file and
     * proxies writes through to the underlying file.
     *
     * <p>Upon calling {@link SafeOutputStream#commit()} the rest of the safety behaviors kick in:
     *
     * <ul>
     *   <li>flush
     *   <li>fsync temp file
     *   <li>close temp file
     *   <li>atomic rename temp file to desired filename
     *   <li>fsync parent directory
     * </ul>
     *
     * <p>On error it will make a best-effort to erase the temporary file.
     *
     * <p>If you call {@link SafeOutputStream#close()} without calling {@link
     * SafeOutputStream#commit()} the atomic write is aborted and cleaned up.
     *
     * <p>It is safe to call {@link SafeOutputStream#close()}} after {@link
     * SafeOutputStream#commit()} so that try-with-resources works.
     *
     * <p>The returned {@link SafeOutputStream} is NOT safe for calls from multiple threads.
     *
     * @param path final desired output path
     * @return handle to opened temp file
     * @throws IOException in the event that the file could not be created.
     */
    @NonNull
    public static SafeOutputStream createAtomicFile(final Path path) throws IOException {
        final Path dir = path.getParent();
        final Path name = path.getFileName();
        final Path tempFile =
                Files.createTempFile(
                        dir,
                        name.toString(),
                        ".tmp",
                        PosixFilePermissions.asFileAttribute(
                                PosixFilePermissions.fromString("rw-r--r--")));

        FileChannel fc = null;
        try {
            fc =
                    (FileChannel)
                            Files.newByteChannel(
                                    tempFile,
                                    StandardOpenOption.CREATE,
                                    StandardOpenOption.TRUNCATE_EXISTING,
                                    StandardOpenOption.WRITE);
        } catch (Exception e) {
            // clean up after ourselves on error
            deleteIfExistsQuietly(tempFile);

            //noinspection ConstantConditions
            if (fc != null) {
                Closeables2.close(fc);
            }

            Throwables.propagateIfInstanceOf(e, IOException.class);
            throw Throwables.propagate(e);
        }

        return new SafeFileOutputStream(path, tempFile, fc);
    }

    /**
     * Delete a path but do not complain if it fails.
     *
     * @param path path to delete
     */
    public static void deleteIfExistsQuietly(final Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException deleteExc) {
            /* ignore */
        }
    }

    /**
     * @see SafeFiles#createAtomicFile(Path)
     *     <p>NotThreadSafe
     */
    @ParametersAreNonnullByDefault
    private static class SafeFileOutputStream extends SafeOutputStream {
        @NonNull private final Path path;
        @NonNull private final Path tempFile;

        @NonNull private final OutputStream out; // do not close this
        @NonNull private final FileChannel fileChannel; // only close this

        private boolean closed = false;

        // private to require you to use SafeFiles.createAtomicFile()
        private SafeFileOutputStream(
                final Path path, final Path tempFile, final FileChannel fileChannel) {
            this.path = path;
            this.tempFile = tempFile;
            this.fileChannel = fileChannel;
            this.out = Channels.newOutputStream(fileChannel);
        }

        /** {@inheritDoc} */
        @Override
        public void commit() throws IOException {
            if (closed) {
                return;
            }

            try {
                try {
                    out.flush();
                    fileChannel.force(true); // fsync
                } finally {
                    fileChannel.close();
                }

                Files.move(tempFile, path, StandardCopyOption.ATOMIC_MOVE);

            } catch (Exception e) {
                // clean up after ourselves on error
                deleteIfExistsQuietly(tempFile);

                Throwables.propagateIfInstanceOf(e, IOException.class);
                closed = true;
                throw Throwables.propagate(e);
            }

            closed = true;

            // Fsync the parent directory inode as well. If this fails we
            // don't have FS cleanup to do, really.
            fsync(tempFile.getParent());
        }

        /** {@inheritDoc} */
        @Override
        public void close() throws IOException {
            if (!closed) {
                Closeables2.close(fileChannel);
                deleteIfExistsQuietly(tempFile);

                closed = true;
            }
        }

        @Override
        public boolean isOpen() {
            return (!closed);
        }

        /** {@inheritDoc} */
        @Override
        public int write(final ByteBuffer src) throws IOException {
            return writeFully(fileChannel, src);
        }

        /**
         * (copied from {@link Channels#writeFullyImpl} and changed to return the number of bytes
         * written)
         *
         * <p>Write all remaining bytes in buffer to the given channel. If the channel is selectable
         * then it must be configured blocking.
         */
        private static int writeFully(final WritableByteChannel ch, final ByteBuffer bb)
                throws IOException {
            int total = 0;
            while (bb.remaining() > 0) {
                int n = ch.write(bb);
                if (n <= 0) {
                    throw new RuntimeException("no bytes written");
                }
                total += n;
            }
            return total;
        }

        @Override
        public void write(int b) throws IOException {
            checkNotClosed();
            out.write(b);
        }

        @Override
        public void write(byte[] b) throws IOException {
            checkNotClosed();
            out.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            checkNotClosed();
            out.write(b, off, len);
        }

        @Override
        public void flush() throws IOException {
            checkNotClosed();
            out.flush();
        }

        private void checkNotClosed() throws IllegalStateException {
            if (closed) {
                throw new IllegalStateException("operation not permitted once output is closed");
            }
        }
    }

    private SafeFiles() {
        /* no */
    }
}
