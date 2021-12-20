package com.indeed.util.serialization.array;

import com.indeed.util.serialization.LengthVIntSerializer;
import com.indeed.util.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Array;

/**
 * @author jplaisance
 */
public final class ObjectArraySerializer<E> implements Serializer<E[]> {
    private static final Logger log = LoggerFactory.getLogger(ObjectArraySerializer.class);

    private static final LengthVIntSerializer lengthSerializer = new LengthVIntSerializer();

    private final Serializer<E> serializer;
    private final Class<E> type;

    public ObjectArraySerializer(Serializer<E> serializer, Class<E> type) {
        this.serializer = serializer;
        this.type = type;
    }

    @Override
    public void write(E[] es, DataOutput out) throws IOException {
        lengthSerializer.write(es.length, out);
        for (E e : es) {
            serializer.write(e, out);
        }
    }

    @Override
    public E[] read(DataInput in) throws IOException {
        final int length = lengthSerializer.read(in);
        final E[] es = (E[]) Array.newInstance(type, length);
        for (int i = 0; i < es.length; i++) {
            es[i] = serializer.read(in);
        }
        return es;
    }
}
