package com.indeed.util.io.checkpointer;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.io.Files;
import com.indeed.util.io.BufferedFileDataOutputStream;
import com.indeed.util.serialization.Stringifier;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;

/**
 * @author tony
 */
public class FileBasedCheckpointer<T> implements Checkpointer<T> {

    private final File checkpointFile;
    private final Stringifier<T> stringifier;
    private volatile T value;

    public FileBasedCheckpointer(
            @Nonnull final File checkpointFile,
            @Nonnull Stringifier<T> stringifier,
            @Nonnull T defaultValue
    ) throws IOException {
        this.checkpointFile = Preconditions.checkNotNull(checkpointFile, "no checkpoint file");
        this.stringifier = Preconditions.checkNotNull(stringifier, "no stringifier");
        if (this.checkpointFile.exists()) {
            value = stringifier.fromString(Files.readFirstLine(this.checkpointFile, Charsets.UTF_8));
        }

        if (value == null) {
            value = defaultValue;
        }
    }

    @Override
    public T getCheckpoint() {
        return value;
    }

    @Override
    public synchronized void setCheckpoint(T checkpoint) throws IOException {
        final File checkpointFileNext = new File(checkpointFile.getParent(), checkpointFile.getName() + ".next");
        BufferedFileDataOutputStream out = new BufferedFileDataOutputStream(checkpointFileNext);
        out.write(stringifier.toString(checkpoint).getBytes(Charsets.UTF_8));
        out.sync();
        out.close();
        if (!checkpointFileNext.renameTo(checkpointFile)) {
            throw new IOException("failed to rename " + checkpointFileNext + " to " + checkpointFile);
        }
        value = checkpoint;
    }
}
