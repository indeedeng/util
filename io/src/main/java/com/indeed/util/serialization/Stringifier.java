package com.indeed.util.serialization;

/**
 * @author jplaisance
 */
public interface Stringifier<T> {
    public String toString(T t);

    public T fromString(String str);
}
