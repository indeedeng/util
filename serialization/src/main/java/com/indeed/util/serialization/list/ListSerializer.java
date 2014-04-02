package com.indeed.util.serialization.list;

import com.google.common.base.Supplier;
import com.indeed.util.serialization.CollectionSuppliers;
import com.indeed.util.serialization.LengthVIntSerializer;
import com.indeed.util.serialization.Serializer;
import org.apache.log4j.Logger;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.List;

/**
 * @author jplaisance
 */
public final class ListSerializer<T> implements Serializer<List<T>> {
    private static final Logger log = Logger.getLogger(ListSerializer.class);

    private static final LengthVIntSerializer lengthSerializer = new LengthVIntSerializer();

    public static <T> ListSerializer<T> arrayListSerializer(Serializer<T> tSerializer) {
        return new ListSerializer<T>(new CollectionSuppliers.ArrayListSupplier<T>(), tSerializer);
    }

    private final Supplier<List<T>> listSupplier;
    
    private final Serializer<T> tSerializer;

    public ListSerializer(Supplier<List<T>> listSupplier,Serializer<T> tSerializer) {
        this.listSupplier = listSupplier;
        this.tSerializer = tSerializer;
    }

    @Override
    public void write(List<T> list, DataOutput out) throws IOException {
        lengthSerializer.write(list.size(), out);
        for (T t : list) {
            tSerializer.write(t, out);
        }
    }

    @Override
    public List<T> read(DataInput in) throws IOException {
        final List<T> ret = listSupplier.get();
        final int size = lengthSerializer.read(in);
        for (int i = 0; i < size; i++) {
            ret.add(tSerializer.read(in));
        }
        return ret;
    }
}
