package com.indeed.util.core.reference;

import com.google.common.base.Function;
import com.google.common.io.Closeables;
//import com.indeed.common.util.io.Closeables2;
import org.apache.log4j.Logger;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.IOException;

/**
 * This class is a mutable reference to a refcounted object. To access the referenced object, call getCopy() which
 * returns a SharedReference<T> which has a get() method which will return the object. The SharedReference<T> returned
 * by getCopy() must be closed when you are done with it.
 *
 * @author jplaisance
 */
public final class AtomicSharedReference<T> {
    private static final Logger log = Logger.getLogger(AtomicSharedReference.class);

    public static <T> AtomicSharedReference<T> create() {
        return new AtomicSharedReference<T>();
    }

    public static <T extends Closeable> AtomicSharedReference<T> create(T t) {
        return new AtomicSharedReference<T>(t, t);
    }

    public static <T> AtomicSharedReference<T> create(T t, Closeable closeable) {
        return new AtomicSharedReference<T>(t, closeable);
    }

    private SharedReference<T> ref;

    private AtomicSharedReference() {
        ref = null;
    }

    private AtomicSharedReference(T t, Closeable closeable) {
        ref = SharedReference.create(t, closeable);
    }

    public synchronized @Nullable SharedReference<T> getAndSet(T t) {
        return getAndSet(t, (Closeable)t);
    }

    public synchronized @Nullable SharedReference<T> getAndSet(T t, Closeable closeable) {
        if ((ref != null) && (ref.get() == t)) return ref.copy();  // If we've been told to set the object we already have, return a copy
        final SharedReference<T> ret = ref;
        ref = SharedReference.create(t, closeable);
        return ret;
    }

    /**
     * use getCopy() instead
     * @return a copy of the reference to the object currently held by this AtomicSharedReference
     */
    public @Deprecated @Nullable SharedReference<T> get() {
        return getCopy();
    }

    /**
     * @return a copy of the reference to the object currently held by this AtomicSharedReference
     */
    public synchronized @Nullable SharedReference<T> getCopy() {
        if (ref == null) return null;
        return ref.copy();
    }

    public synchronized void set(T t) throws IOException {
        set(t, (Closeable)t);
    }

    /**
     * Like set(), but quietly closes any previous reference.
     */
    public synchronized void setQuietly(T t) {
        if ((ref != null) && (ref.get() == t)) return;  // If we've been told to set to the object we are already tracking, this is a no-op.
        this.unsetQuietly();
        ref = SharedReference.create(t, (Closeable)t);
    }

    public synchronized void set(T t, Closeable closeable) throws IOException {
        if ((ref != null) && (ref.get() == t)) return;  // If we've been told to set to the object we are already tracking, this is a no-op.
        if (ref != null) ref.close();
        ref = SharedReference.create(t, closeable);
    }

    public synchronized void unset() throws IOException {
        if (ref != null) ref.close();
        ref = null;
    }

    /**
     * Unsets the reference, closing with Closeables2.closeQuietly().
     */
    public synchronized void unsetQuietly() {
        //if (ref != null) Closeables2.closeQuietly(ref, log);
        if (ref != null) Closeables.closeQuietly(ref);
        ref = null;
    }

    public synchronized @Nullable SharedReference<T> getAndUnset() {
        final SharedReference<T> ret = ref;
        ref = null;
        return ret;
    }

    /**
     * Return the ref count for the current managed reference.  Or 0 if unset.
     * For debugging and logging.
     * @return ref count.
     */
    public synchronized int getRefCount() {
        if (ref == null) return 0;
        return ref.getRefCount();
    }

    /**
     * Call some function f on the reference we are storing.
     * Saving the value of T after this call returns is COMPLETELY UNSAFE.  Don't do it.
     *
     * @param f  lambda(T x)
     * @param <Z> Return type; <? extends Object>
     * @return result of f
     */
    public synchronized @Nullable <Z> Z map(Function<T, Z> f) {
        if (ref == null) {
            return f.apply(null);
        } else {
            return f.apply(ref.get());
        }
    }

    /**
     * Call some function f on a threadsafe copy of the reference we are storing.
     * Should be used if you expect the function to take a while to run.
     * Saving the value of T after this call returns is COMPLETELY UNSAFE.  Don't do it.
     *
     * @param f  lambda(T x)
     * @param <Z> Return type; <? extends Object>
     * @return result of f
     * @throws IOException  if closing the local reference throws.
     */
    public @Nullable <Z> Z mapWithCopy(Function<T, Z> f) throws IOException {
        final @Nullable SharedReference<T> localRef = getCopy();
        try {
            if (localRef == null) {
                return f.apply(null);
            } else {
                return f.apply(localRef.get());
            }
        } finally {
            if (localRef != null) localRef.close();
        }
    }

    /**
     * Just like mapWithCopy() except that this silently swallows any exception that calling close() on the copy might throw.
     * @param f
     * @param <Z>
     * @return
     */
    public @Nullable <Z> Z mapWithCopyQuietly(Function<T, Z> f) {
        final @Nullable SharedReference<T> localRef = getCopy();
        try {
            if (localRef == null) {
                return f.apply(null);
            } else {
                return f.apply(localRef.get());
            }
        } finally {
            //if (localRef != null) Closeables2.closeQuietly(localRef, log);
            if (localRef != null) Closeables.closeQuietly(localRef);
        }
    }
}
