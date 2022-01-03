package com.indeed.util.core.reference;

import com.google.common.base.Supplier;
import com.indeed.util.core.io.Closeables2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.IOException;

/**
 * @author jplaisance
 */
public final class ReloadableSharedReference<T, E extends Throwable> {

    private static final Logger log = LoggerFactory.getLogger(ReloadableSharedReference.class);

    public static <T extends Closeable, E extends Throwable> ReloadableSharedReference<T, E> create(Loader<T, E> loader) {
        return new ReloadableSharedReference<T, E>(loader, Closer.<T>closeableCloser());
    }

    public static <T, E extends Throwable> ReloadableSharedReference<T, E> create(Loader<T, E> loader, Closer<T> closer) {
        return new ReloadableSharedReference<T, E>(loader, closer);
    }

    private final Loader<T, E> loader;

    private final Closer<T> closer;

    private volatile WeakSharedReference<T> weakRef;

    public ReloadableSharedReference(Loader<T, E> loader, Closer<T> closer) {
        this.loader = loader;
        this.closer = closer;
    }

    public static abstract class Closer<T> {

        private static final Closer<Closeable> closeableCloser = new Closer<Closeable>() {
            @Override
            public void close(final Closeable closeable) {
                Closeables2.close(closeable);
            }
        };

        private static <T extends Closeable> Closer<T> closeableCloser() {
            return (Closer<T>)closeableCloser;
        }

        public abstract void close(T t);

        public final Closeable asCloseable(final T t) {
            return new Closeable() {
                @Override
                public void close() throws IOException {
                    Closer.this.close(t);
                }
            };
        }
    }

    public static abstract class Loader<T, E extends Throwable> {

        public static <T> Loader<T, RuntimeException> fromSupplier(final Supplier<T> supplier) {
            return new Loader<T, RuntimeException>() {
                @Override
                public T load() {
                    return supplier.get();
                }
            };
        }

        public abstract T load() throws E;
    }

    public SharedReference<T> copy() throws E {
        while (true) {
            final WeakSharedReference<T> w = weakRef;
            if (w != null) {
                final SharedReference<T> ret = w.tryCopy();
                if (ret != null) {
                    return ret;
                }
            }
            synchronized (loader) {
                if (w == weakRef) {
                    final T t = loader.load();
                    final SharedReference<T> ret = SharedReference.create(t, new Closeable() {
                        @Override
                        public void close() throws IOException {
                            synchronized (loader) {
                                weakRef = null;
                                closer.close(t);
                            }
                        }
                    });
                    weakRef = WeakSharedReference.create(ret);
                    return ret;
                }
            }
        }
    }

    public @Nullable SharedReference<T> copyIfLoaded() {
        final WeakSharedReference<T> w = weakRef;
        if (w != null) {
            return w.tryCopy();
        }
        return null;
    }
}
