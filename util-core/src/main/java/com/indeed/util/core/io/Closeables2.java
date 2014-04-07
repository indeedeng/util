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
     * @param closeables closeables to close in bulk
     * @param log logger to use
     */
    public static void closeAll(@Nonnull final Iterable<? extends Closeable> closeables, @Nonnull final Logger log) {
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
     * same as above but varargs
     */
    public static void closeAll(@Nonnull final Logger log, @Nonnull Closeable... closeables) {
        closeAll(Arrays.asList(closeables), log);
    }

    /**
     * same as above with the parameters flipped for consistency with varargs variant
     */
    public static void closeAll(@Nonnull final Logger log, @Nonnull final Iterable<? extends Closeable> closeables) {
        closeAll(closeables, log);
    }

    /**
     * use this to get an exception safe Closeable object that closes everything in this Iterable<? extends Closeable>.
     *
     * this is needed because consecutive closeAll calls are not exception safe without an extra layer of try/finally
     */
    public static <C extends Closeable> Closeable forIterable(@Nonnull final Logger log, @Nonnull final Iterable<C> closeables) {
        return new Closeable() {
            public void close() throws IOException {
                closeAll(log, closeables);
            }
        };
    }

    /**
     * use this to get an exception safe Closeable object that closes everything in this Iterable<? extends Iterable<? extends Closeable>>.
     *
     * this is needed because consecutive closeAll calls are not exception safe without an extra layer of try/finally
     */
    public static <C extends Closeable, T extends Iterable<C>> Closeable forIterable2(@Nonnull final Logger log, @Nonnull final Iterable<T> closeables) {
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
     * use this to get an exception safe Closeable object that closes everything in closeables.
     *
     * this is needed because consecutive closeAll calls are not exception safe without an extra layer of try/finally
     */
    public static Closeable forArray(@Nonnull final Logger log, @Nonnull final Closeable... closeables) {
        return new Closeable() {
            public void close() throws IOException {
                closeAll(log, closeables);
            }
        };
    }
}
