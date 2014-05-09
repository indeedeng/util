package com.indeed.util.core;

import com.google.common.base.Throwables;

/**
 * @author jplaisance
 */
public final class Throwables2 {

    public static <X extends Throwable> RuntimeException propagate(Throwable t, Class<X> xClass) throws X {
        Throwables.propagateIfInstanceOf(t, xClass);
        throw Throwables.propagate(t);
    }

    public static <X1 extends Throwable, X2 extends Throwable> RuntimeException propagate(Throwable t, Class<X1> x1Class, Class<X2> x2Class) throws X1, X2 {
        Throwables.propagateIfInstanceOf(t, x1Class);
        Throwables.propagateIfInstanceOf(t, x2Class);
        throw Throwables.propagate(t);
    }

    public static <X1 extends Throwable, X2 extends Throwable, X3 extends Throwable> RuntimeException propagate(Throwable t, Class<X1> x1Class, Class<X2> x2Class, Class<X3> x3Class) throws X1, X2, X3 {
        Throwables.propagateIfInstanceOf(t, x1Class);
        Throwables.propagateIfInstanceOf(t, x2Class);
        Throwables.propagateIfInstanceOf(t, x3Class);
        throw Throwables.propagate(t);
    }
}
