// Copyright 2015 Indeed
package com.indeed.util.io;

import com.google.common.collect.Iterables;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilities for working with directory {@link Path} objects.
 *
 * @author rboyer
 */
public final class Directories {
    /**
     * Count the number of entries in a directory.
     *
     * @param dir directory to evaluate
     * @return number of inodes under it.
     * @throws IOException
     */
    public static int count(@NonNull final Path dir) throws IOException {
        try (final DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            return Iterables.size(stream);
        } catch (DirectoryIteratorException ex) {
            // I/O error encounted during the iteration, the cause is an IOException
            throw ex.getCause();
        }
    }

    /**
     * Convenience method to return all paths in a SMALL directory.
     *
     * <p>DO NOT USE THIS TO TRAVERSE LARGE (multi-thousand inode) DIRECTORIES!
     *
     * <p>For starters you shouldn't be making directories that big at all, but if you did please
     * use {@link Files#newDirectoryStream(Path)} directly in your code.
     *
     * @param dir directory to evaluate
     * @return all files in that directory
     * @throws IOException
     */
    @NonNull
    public static List<Path> list(@NonNull final Path dir) throws IOException {
        final List<Path> contents = new ArrayList<>();
        try (final DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (final Path entry : stream) {
                contents.add(entry);
            }
        } catch (DirectoryIteratorException ex) {
            // I/O error encounted during the iteration, the cause is an IOException
            throw ex.getCause();
        }
        return contents;
    }

    private Directories() {
        /* no */
    }
}
