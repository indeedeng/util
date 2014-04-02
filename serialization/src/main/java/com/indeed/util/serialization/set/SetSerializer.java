package com.indeed.util.serialization.set;

import com.google.common.base.Supplier;
import com.indeed.util.serialization.CollectionSuppliers;
import com.indeed.util.serialization.LengthVIntSerializer;
import com.indeed.util.serialization.Serializer;
import org.apache.log4j.Logger;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Set;

/**
 * @author jplaisance
 */
public final class SetSerializer<E> implements Serializer<Set<E>> {
    private static final Logger log = Logger.getLogger(SetSerializer.class);

    private static final LengthVIntSerializer lengthSerializer = new LengthVIntSerializer();
    
    public static <E> SetSerializer<E> hashSetSerializer(Serializer<E> serializer) {
        return new SetSerializer<E>(new CollectionSuppliers.HashSetSupplier(), serializer);
    }

    private final Serializer<E> elementSerializer;
    private final Supplier<Set<E>> setSupplier;

    public SetSerializer(Supplier<Set<E>> setSupplier, Serializer<E> elementSerializer) {
        this.elementSerializer = elementSerializer;
        this.setSupplier = setSupplier;
    }

    @Override
    public void write(Set<E> es, DataOutput out) throws IOException {
        lengthSerializer.write(es.size(), out);
        for (E e : es) {
            elementSerializer.write(e, out);
        }
    }

    @Override
    public Set<E> read(DataInput in) throws IOException {
        final int length = lengthSerializer.read(in);
        Set<E> ret = setSupplier.get();
        for (int i = 0; i < length; i++) {
            ret.add(elementSerializer.read(in));
        }
        return ret;
    }
}
