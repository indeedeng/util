package com.indeed.util.core.io;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import org.apache.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;

/**
 * This augments guava'a {@link com.google.common.io.Closeables}
 *
 * @author rboyer
 */
public class Closeables2 {
    /**
     * this is very similar to guava's {#link Closeables.closeQuietly()}, except with logging
     * unlike guava this swallows all Exceptions, not just IOExceptions. Error is still propagated.
     * @param closeable closeable to close
     * @param log logger to use
     */
    public static void closeQuietly(@Nullable final Closeable closeable, @Nonnull final Logger log) {
        try {
            if (null != closeable) {
                closeable.close();
            }
        } catch (Exception e) {
            log.error("Exception during cleanup of a Closeable, ignoring", e);
        }
    }

    /**
     * Close all {@link Closeable} objects provided in the {@code closeables}
     * iterator. When encountering an error when closing, write the message out
     * to the provided {@code log}.
     *
     * @param closeables The closeables that we want to close.
     * @param log The log where we will write error messages when failing to
     *            close a closeable.
     */
    public static void closeAll(
            @Nonnull final Iterable<? extends Closeable> closeables,
            @Nonnull final Logger log
    ) {
        Throwable throwable = null;
        for (Closeable closeable : closeables) {
            try {
                closeQuietly(closeable, log);
            } catch (Throwable e) {
                if (throwable == null) {
                    throwable = e;
                } else {
                    log.error("Suppressing throwable thrown when closing "+closeable, e);
                }
            }
        }
        if (throwable != null) {
            throw Throwables.propagate(throwable);
        }
    }

    /**
     * Close all {@link Closeable} objects provided in the {@code closeables}
     * iterator. When encountering an error when closing, write the message out
     * to the provided {@code log}.
     *
     * @param log The log where we will write error messages when failing to
     *            close a closeable.
     * @param closeables The closeables that we want to close.
     */
    public static void closeAll(
            @Nonnull final Logger log,
            @Nonnull final Closeable... closeables
    ) {
        closeAll(Arrays.asList(closeables), log);
    }

    /**
     * Close all {@link Closeable} objects provided in the {@code closeables}
     * iterator. When encountering an error when closing, write the message out
     * to the provided {@code log}. This method is a helper function that is
     * intended to provide a similar method signature to the var-args variant.
     *
     * @param log The log where we will write error messages when failing to
     *            close a closeable.
     * @param closeables The closeables that we want to close.
     */
    public static void closeAll(
            @Nonnull final Logger log,
            @Nonnull final Iterable<? extends Closeable> closeables
    ) {
        closeAll(closeables, log);
    }

    /**
     * Create a composite {@link Closeable} that will close all underlying
     * {@code closeables} references. The {@link Closeable#close()} method will
     * perform a safe close of all wrapped Closeable objects. This is needed
     * since successive calls of the {@link #closeAll(Logger, Closeable...)}
     * method is not exception safe without an extra layer of try / finally.
     *
     * @param log The logger to write error messages out to.
     * @param closeables The closeables to shutdown when the composite is closed.
     * @param <C> A class that implements the {@link Closeable} interface.
     * @return A composite Closeable that wraps all underlying {@code closeables}.
     */
    public static <C extends Closeable> Closeable forIterable(
            @Nonnull final Logger log,
            @Nonnull final Iterable<C> closeables
    ) {
        return new Closeable() {
            public void close() throws IOException {
                closeAll(log, closeables);
            }
        };
    }

    /**
     * Create a composite {@link Closeable} that will close all the wrapped
     * {@code closeables} references. The {@link Closeable#close()} method will
     * perform a safe close of all wrapped Closeable objects. This is needed
     * since successive calls of the {@link #closeAll(Logger, Closeable...)}
     * method is not exception safe without an extra layer of try / finally.
     *
     * This method iterates the provided
     * {@code Iterable<? extends Iterable<? extends Closeable>>} and closes
     * all subsequent Closeable objects.
     *
     * @param log The logger to write error messages out to.
     * @param closeables An iterator over iterable Closeables.
     * @param <C> A class that implements the {@link Closeable} interface.
     * @param <T> An iterable collection that contains {@link Closeable} objects.
     * @return A composite closeable that wraps all underlying {@code closeables}.
     */
    public static <C extends Closeable, T extends Iterable<C>> Closeable forIterable2(
            @Nonnull final Logger log,
            @Nonnull final Iterable<T> closeables
    ) {
        return new Closeable() {
            public void close() throws IOException {
                closeAll(log, Iterables.transform(closeables, new Function<T, Closeable>() {
                    public Closeable apply(final @Nullable T input) {
                        if (input == null) {
                            return new Closeable() {
                                public void close() throws IOException {}
                            };
                        } else {
                            return forIterable(log, input);
                        }
                    }
                }));
            }
        };
    }

    /**
     * Create a composite {@link Closeable} that will close all underlying
     * {@code closeables} references. The {@link Closeable#close()} method will
     * perform a safe close of all wrapped Closeable objects. This is needed
     * since successive calls of the {@link #closeAll(Logger, Closeable...)}
     * method is not exception safe without an extra layer of try / finally.
     *
     * @param log The logger to write error messages out to.
     * @param closeables The closeables to shutdown when the composite is closed.
     * @return A composite Closeable that wraps all underlying {@code closeables}.
     */
    public static Closeable forArray(
            @Nonnull final Logger log,
            @Nonnull final Closeable... closeables
    ) {
        return new Closeable() {
            public void close() throws IOException {
                closeAll(log, closeables);
            }
        };
    }
}
