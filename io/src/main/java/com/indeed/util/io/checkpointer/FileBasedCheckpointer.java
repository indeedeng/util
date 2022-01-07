package com.indeed.util.io.checkpointer;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.indeed.util.io.BufferedFileDataOutputStream;
import com.indeed.util.serialization.Stringifier;

import javax.annotation.Nonnull;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

/** @author tony */
public class FileBasedCheckpointer<T> implements Checkpointer<T> {

    private final Path checkpointFilePath;
    private final Stringifier<T> stringifier;
    private volatile T value;

    public FileBasedCheckpointer(
            @Nonnull final File checkpointFile,
            @Nonnull Stringifier<T> stringifier,
            @Nonnull T defaultValue)
            throws IOException {
        this(checkpointFile.toPath(), stringifier, defaultValue);
    }

    public FileBasedCheckpointer(
            @Nonnull final Path checkpointFilePath,
            @Nonnull Stringifier<T> stringifier,
            @Nonnull T defaultValue)
            throws IOException {
        this.checkpointFilePath =
                Preconditions.checkNotNull(checkpointFilePath, "no checkpoint file");
        this.stringifier = Preconditions.checkNotNull(stringifier, "no stringifier");
        if (Files.exists(this.checkpointFilePath)) {
            try (final BufferedReader bufferedReader =
                    Files.newBufferedReader(this.checkpointFilePath, Charsets.UTF_8)) {
                final String line = bufferedReader.readLine();
                if (line != null) {
                    value = stringifier.fromString(line);
                }
            }
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
        final Path checkpointFileDir = checkpointFilePath.getParent();
        final Path checkpointFilePathNext =
                checkpointFileDir.resolve(checkpointFilePath.getFileName() + ".next");
        try (BufferedFileDataOutputStream out =
                new BufferedFileDataOutputStream(checkpointFilePathNext)) {
            out.write(stringifier.toString(checkpoint).getBytes(Charsets.UTF_8));
            out.sync();
        }

        try {
            Files.move(
                    checkpointFilePathNext,
                    checkpointFilePath,
                    StandardCopyOption.REPLACE_EXISTING);
            try (FileChannel dirChannel =
                    FileChannel.open(checkpointFileDir, StandardOpenOption.READ)) {
                dirChannel.force(true);
            }
        } catch (final IOException e) {
            throw new IOException(
                    "failed to rename " + checkpointFilePathNext + " to " + checkpointFilePath, e);
        }

        value = checkpoint;
    }
}
