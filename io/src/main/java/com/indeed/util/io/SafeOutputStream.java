// Copyright 2015 Indeed
package com.indeed.util.io;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Path;

/**
 * @see SafeFiles#createAtomicFile(Path)
 *     <p>Not Thread Safe
 */
public abstract class SafeOutputStream extends OutputStream
        implements WritableByteChannel, Closeable {
    /**
     * Writes a sequence of bytes to this channel from the given buffer.
     *
     * <p>Unlike the contract of {@link WritableByteChannel}, the ENTIRE buffer is written to the
     * channel (behaving like writeFully).
     *
     * @param src The buffer to flush.
     * @return The number of bytes written to the output stream.
     * @throws IOException in the event that the buffer could not be written.
     */
    @Override
    public abstract int write(@NonNull final ByteBuffer src) throws IOException;

    /**
     * Commit causes the current atomic file writing operation to conclude and the current temp file
     * is safely promoted to being the canonical file.
     *
     * <p>It is safe to call {@link #close()}} after commit. It is NOT safe to call any of the
     * variations on {@link #write} or {@link #flush()} however.
     *
     * @throws IOException in the event that the data could not be committed.
     */
    public abstract void commit() throws IOException;

    /**
     * If {@link #commit()} as been called, this method is a NO-OP. Otherwise...
     *
     * <p>Close causes the current atomic file writing operation to abort and the current temp file
     * to be erased. It is NOT safe to call any of the variations on {@link #write} or {@link
     * #flush()} however.
     *
     * @throws IOException in the event that the output stream cannot be closed.
     */
    @Override
    public abstract void close() throws IOException;
}
