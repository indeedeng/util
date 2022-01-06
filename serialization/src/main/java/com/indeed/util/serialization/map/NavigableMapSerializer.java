package com.indeed.util.serialization.map;

import com.google.common.base.Supplier;
import com.indeed.util.serialization.CollectionSuppliers;
import com.indeed.util.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.NavigableMap;

/**
 * @author jplaisance
 */
public final class NavigableMapSerializer<K,V> implements Serializer<NavigableMap<K,V>> {
    private static final Logger log = LoggerFactory.getLogger(NavigableMapSerializer.class);

    public static <K extends Comparable,V> NavigableMapSerializer<K,V> treeMapSerializer(Serializer<K> keySerializer, Serializer<V> valueSerializer) {
        return new NavigableMapSerializer<K, V>(new CollectionSuppliers.TreeMapSupplier<K, V>(), keySerializer, valueSerializer);
    }
    
    private final MapSerializer<K,V> mapSerializer;

    public NavigableMapSerializer(Supplier<? extends NavigableMap<K,V>> mapSupplier, Serializer<K> keySerializer, Serializer<V> valueSerializer) {
        mapSerializer = new MapSerializer<K, V>(mapSupplier, keySerializer, valueSerializer);
    }

    @Override
    public void write(NavigableMap<K, V> map, DataOutput out) throws IOException {
        mapSerializer.write(map, out);
    }

    @Override
    public NavigableMap<K, V> read(DataInput in) throws IOException {
        return (NavigableMap<K, V>) mapSerializer.read(in);
    }
}
