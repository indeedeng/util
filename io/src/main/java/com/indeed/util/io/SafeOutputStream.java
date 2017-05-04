// Copyright 2015 Indeed
package com.indeed.util.io;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Path;

/**
 * @see SafeFiles#createAtomicFile(Path)
 */
@NotThreadSafe
public abstract class SafeOutputStream extends OutputStream implements WritableByteChannel, Closeable {
    /**
     * Writes a sequence of bytes to this channel from the given buffer.
     *
     * <p>
     * Unlike the contract of {@link WritableByteChannel}, the ENTIRE buffer
     * is written to the channel (behaving like writeFully).
     * </p>
     *
     * @param src The buffer to flush.
     * @return The number of bytes written to the output stream.
     * @throws IOException in the event that the buffer could not be written.
     */
    @Override
    public abstract int write(@Nonnull final ByteBuffer src) throws IOException;

    /**
     * Commit causes the current atomic file writing operation to conclude
     * and the current temp file is safely promoted to being the canonical file.
     *
     * <p>
     * It is safe to call {@link #close()}} after commit.
     * </p>
     *
     * It is NOT safe to call any of the variations on {@link #write} or {@link #flush()} however.
     * @throws IOException in the event that the data could not be committed.
     */
    public abstract void commit() throws IOException;

    /**
     * If {@link #commit()} as been called, this method is a NO-OP. Otherwise...
     *
     * <p>
     * Close causes the current atomic file writing operation to abort and the
     * current temp file to be erased.
     * </p>
     *
     * It is NOT safe to call any of the variations on {@link #write} or {@link #flush()} however.
     * @throws IOException in the event that the output stream cannot be closed.
     */
    @Override
    public abstract void close() throws IOException;
}
