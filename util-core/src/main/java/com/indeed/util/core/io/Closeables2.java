package com.indeed.util.core.io;

import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Closeable;
import java.util.Arrays;

/**
 * This augments guava'a {@link com.google.common.io.Closeables}
 *
 * @author rboyer
 */
public class Closeables2 {
    public static final Logger log = LoggerFactory.getLogger(Closeables2.class);

    /**
     * this is very similar to guava's {#link Closeables.closeQuietly()}, except with logging
     * unlike guava this swallows all Exceptions, not just IOExceptions. Error is still propagated.
     * @param closeable closeable to close
     */
    public static void closeQuietly(@Nullable final Closeable closeable) {
        try {
            if (null != closeable) {
                closeable.close();
            }
        } catch (Exception e) {
            log.error("Exception during cleanup of a Closeable, ignoring", e);
        }
    }

    /**
     * @deprecated Use {@link #closeQuietly(java.io.Closeable)}
     */
    @Deprecated
    public static void closeQuietly(@Nullable final Closeable closeable, @Nonnull final org.apache.log4j.Logger log) {
        closeQuietly(closeable);
    }

    /**
     * Close all {@link Closeable} objects provided in the {@code closeables}
     * iterator. When encountering an error when closing, write the message out
     * to the provided {@code log}.
     *
     * @param closeables The closeables that we want to close.
     */
    public static void closeAll(
            @Nonnull final Iterable<? extends Closeable> closeables
    ) {
        Throwable throwable = null;
        for (Closeable closeable : closeables) {
            try {
                closeQuietly(closeable);
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
     * @deprecated Use {@link #closeAll(java.lang.Iterable)}
     */
    @Deprecated
    public static void closeAll(
            @Nonnull final Iterable<? extends Closeable> closeables,
            @Nonnull final org.apache.log4j.Logger log
    ) {
        closeAll(closeables);
    }

    /**
     * Close all {@link Closeable} objects provided in the {@code closeables}
     * iterator. When encountering an error when closing, write the message out
     * to the provided {@code log}.
     *
     * @param closeables The closeables that we want to close.
     */
    public static void closeAll(
            @Nonnull final Closeable... closeables
    ) {
        closeAll(Arrays.asList(closeables));
    }

    /**
     * @deprecated Use {@link #closeAll(java.io.Closeable...)}
     */
    @Deprecated
    public static void closeAll(
            @Nonnull final org.apache.log4j.Logger log,
            @Nonnull final Closeable... closeables
    ) {
        closeAll(closeables);
    }

    /**
     * @deprecated Use {@link #closeAll(java.io.Closeable...)}
     */
    @Deprecated
    public static void closeAll(
            @Nonnull final org.apache.log4j.Logger log,
            @Nonnull final Iterable<? extends Closeable> closeables
    ) {
        closeAll(closeables);
    }

    /**
     * Create a composite {@link Closeable} that will close all underlying
     * {@code closeables} references. The {@link Closeable#close()} method will
     * perform a safe close of all wrapped Closeable objects. This is needed
     * since successive calls of the {@link #closeAll(Closeable...)}
     * method is not exception safe without an extra layer of try / finally.
     *
     * @param closeables The closeables to shutdown when the composite is closed.
     * @param <C> A class that implements the {@link Closeable} interface.
     * @return A composite Closeable that wraps all underlying {@code closeables}.
     */
    public static <C extends Closeable> Closeable forIterable(
            @Nonnull final Iterable<C> closeables
    ) {
        return () -> closeAll(closeables);
    }

    /**
     * @deprecated Use {@link #forIterable(java.lang.Iterable)}
     */
    @Deprecated
    public static <C extends Closeable> Closeable forIterable(
            @Nonnull final org.apache.log4j.Logger log,
            @Nonnull final Iterable<C> closeables
    ) {
        return forIterable(closeables);
    }

    /**
     * Create a composite {@link Closeable} that will close all the wrapped
     * {@code closeables} references. The {@link Closeable#close()} method will
     * perform a safe close of all wrapped Closeable objects. This is needed
     * since successive calls of the {@link #closeAll(Closeable...)}
     * method is not exception safe without an extra layer of try / finally.
     *
     * This method iterates the provided
     * {@code Iterable<? extends Iterable<? extends Closeable>>} and closes
     * all subsequent Closeable objects.
     *
     * @param closeables An iterator over iterable Closeables.
     * @param <C> A class that implements the {@link Closeable} interface.
     * @param <T> An iterable collection that contains {@link Closeable} objects.
     * @return A composite closeable that wraps all underlying {@code closeables}.
     */
    public static <C extends Closeable, T extends Iterable<C>> Closeable forIterable2(
            @Nonnull final Iterable<T> closeables
    ) {
        return () -> closeAll(Iterables.transform(closeables, input -> {
            if (input == null) {
                return () -> {};
            } else {
                return forIterable(input);
            }
        }));
    }

    /**
     * @deprecated Use {@link #forIterable2(java.lang.Iterable)}
     */
    @Deprecated
    public static <C extends Closeable, T extends Iterable<C>> Closeable forIterable2(
            @Nonnull final org.apache.log4j.Logger log,
            @Nonnull final Iterable<T> closeables
    ) {
        return forIterable2(closeables);
    }

    /**
     * Create a composite {@link Closeable} that will close all underlying
     * {@code closeables} references. The {@link Closeable#close()} method will
     * perform a safe close of all wrapped Closeable objects. This is needed
     * since successive calls of the {@link #closeAll(Closeable...)}
     * method is not exception safe without an extra layer of try / finally.
     *
     * @param closeables The closeables to shutdown when the composite is closed.
     * @return A composite Closeable that wraps all underlying {@code closeables}.
     */
    public static Closeable forArray(
            @Nonnull final Closeable... closeables
    ) {
        return () -> closeAll(closeables);
    }

    /**
     * @deprecated Use {@link #forArray(java.io.Closeable...)}
     */
    @Deprecated
    public static Closeable forArray(
            @Nonnull final org.apache.log4j.Logger log,
            @Nonnull final Closeable... closeables
    ) {
        return forArray(closeables);
    }
}
