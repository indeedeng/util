package com.indeed.util.serialization.map;

import com.google.common.base.Supplier;
import com.indeed.util.serialization.CollectionSuppliers;
import com.indeed.util.serialization.LengthVIntSerializer;
import com.indeed.util.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Map;

/**
 * @author jplaisance
 */
public final class MapSerializer<K,V> implements Serializer<Map<K,V>> {
    private static final Logger log = LoggerFactory.getLogger(MapSerializer.class);

    private static final LengthVIntSerializer lengthSerializer = new LengthVIntSerializer();
    
    public static <K,V> MapSerializer<K,V> hashMapSerializer(Serializer<K> keySerializer, Serializer<V> valueSerializer) {
        return new MapSerializer<K, V>(new CollectionSuppliers.HashMapSupplier<K, V>(), keySerializer, valueSerializer);
    }
    
    private final Supplier<? extends Map<K, V>> mapSupplier;
    private final Serializer<K> keySerializer;
    private final Serializer<V> valueSerializer;

    public MapSerializer(Supplier<? extends Map<K,V>> mapSupplier, Serializer<K> keySerializer, Serializer<V> valueSerializer) {
        this.mapSupplier = mapSupplier;
        this.keySerializer = keySerializer;
        this.valueSerializer = valueSerializer;
    }

    @Override
    public void write(Map<K, V> map, DataOutput out) throws IOException {
        lengthSerializer.write(map.size(), out);
        for (Map.Entry<K, V> entry : map.entrySet()) {
            keySerializer.write(entry.getKey(), out);
            valueSerializer.write(entry.getValue(), out);
        }
    }

    @Override
    public Map<K, V> read(DataInput in) throws IOException {
        Map<K,V> ret = mapSupplier.get();
        final int size = lengthSerializer.read(in);
        for (int i = 0; i < size; i++) {
            ret.put(keySerializer.read(in), valueSerializer.read(in));
        }
        return ret;
    }
}
