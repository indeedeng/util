package com.indeed.util.core.reference;

import org.apache.log4j.Logger;

/**
 * @author jplaisance
 */
public final class WeakSharedReference<T> {

    private static final Logger log = Logger.getLogger(WeakSharedReference.class);

    public static <T> WeakSharedReference<T> create(SharedReference<T> ref) {
        return new WeakSharedReference<T>(ref);
    }

    private final SharedReference<T> ref;

    private WeakSharedReference(final SharedReference<T> ref) {
        this.ref = ref;
    }

    public SharedReference<T> tryCopy() {
        return ref.tryCopy();
    }
}
