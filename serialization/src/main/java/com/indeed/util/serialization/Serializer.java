package com.indeed.util.serialization;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/** @author jplaisance */
public interface Serializer<T> {
    public void write(T t, DataOutput out) throws IOException;

    public T read(DataInput in) throws IOException;
}
