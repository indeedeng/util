package com.indeed.util.core.reference;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * @author jplaisance
 */
public final class WeakSharedReference<T> {

    private static final Logger log = LogManager.getLogger(WeakSharedReference.class);

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
