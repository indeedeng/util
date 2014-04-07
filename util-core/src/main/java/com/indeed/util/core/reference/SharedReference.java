package com.indeed.util.core.reference;

import org.apache.log4j.Logger;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.IOException;

/**
 * This class is intended to make resource tracking slightly easier than direct refcounting. If you want to retain a shared resource
 * that is referenced by this class, copy it and close the copy when you're done. When all copies and the original are closed, the resource
 * will be automatically freed.
 *
 * A particular instance of a SharedReference should be owned by a specific thread. If a reference is intended to be accessed by multiple
 * threads use AtomicSharedReference.
 *
 * You should avoid storing the result of get() in variables so that is isn't accidentally prematurely freed.
 *
 * Once we're using Java 7, this class should always be used with try-with-resources where possible to simplify resource cleanup.
 *
 * @author jplaisance
 */
public final class SharedReference<T> implements Closeable {

    private static final Logger log = Logger.getLogger(SharedReference.class);

    private static final boolean debug = Boolean.getBoolean("com.indeed.common.util.reference.SharedReference.debug");

    private final T t;
    private final SharedReference<T> original;
    private boolean closed = false;

    private final Closeable closeable;
    private int refCount;

    //this exists solely to debug reference leaks. when it is finalized, if the reference has not been closed, it will complain.
    @SuppressWarnings("unused")
    private final ReferenceLeakDebugger referenceLeakDebugger;

    public static <T extends Closeable> SharedReference<T> create(T t) {
        return new SharedReference<T>(t, t);
    }

    public static <T> SharedReference<T> create(T t, Closeable closeable) {
        return new SharedReference<T>(t, closeable);
    }

    private SharedReference(T t, Closeable closeable) {
        this.t = t;
        this.closeable = closeable;
        this.original = this;
        refCount = 1;
        if (debug) {
            referenceLeakDebugger = new ReferenceLeakDebugger();
        } else {
            referenceLeakDebugger = null;
        }
    }

    private SharedReference(SharedReference<T> ref) {
        this.t = ref.t;
        this.original = ref.original;
        this.closeable = null;
        this.refCount = 0;
        if (debug) {
            referenceLeakDebugger = new ReferenceLeakDebugger();
        } else {
            referenceLeakDebugger = null;
        }
    }

    @Override
    public synchronized void close() throws IOException {
        if (closed) return;
        closed = true;
        original.decRef();
    }

    public synchronized SharedReference<T> copy() {
        if (closed) throw new IllegalStateException("cannot copy closed ref");
        if (!original.incRef()) throw new IllegalStateException("resource has been freed");
        return new SharedReference(this);
    }

    public synchronized @Nullable SharedReference<T> tryCopy() {
        if (!original.incRef()) {
            return null;
        }
        return new SharedReference(this);
    }

    public T get() {
        return t;
    }

    // For debugging and logging.
    public synchronized int getRefCount() {
        if (closed) return 0;
        return original.refCount;
    }

    private synchronized boolean incRef() {
        if (refCount <= 0) return false;
        refCount++;
        return true;
    }

    private synchronized void decRef() throws IOException {
        if (refCount <= 0) throw new IllegalStateException("resource has been freed");
        refCount--;
        if (refCount == 0) closeable.close();
    }

    private final class ReferenceLeakDebugger {
        protected void finalize() throws Throwable {
            if (!SharedReference.this.closed) {
                log.error("SharedReference to "+SharedReference.this.t.getClass()+" "+SharedReference.this.t+" was not closed! Closing in finalizer :(");
            }
        }
    }
}
