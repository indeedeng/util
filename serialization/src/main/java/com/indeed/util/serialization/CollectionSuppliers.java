package com.indeed.util.serialization;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author jplaisance
 */
public final class CollectionSuppliers {
    private static final Logger log = LogManager.getLogger(CollectionSuppliers.class);

    public static class HashMapSupplier<K, V> implements Supplier<Map<K, V>> {
        @Override
        public HashMap<K, V> get() {
            return Maps.newHashMap();
        }
    }

    public static class ArrayListSupplier<T> implements Supplier<List<T>> {
        @Override
        public List<T> get() {
            return Lists.newArrayList();
        }
    }

    public static class TreeMapSupplier<K extends Comparable, V> implements Supplier<TreeMap<K, V>> {
        @Override
        public TreeMap<K, V> get() {
            return Maps.newTreeMap();
        }
    }
    
    public static class HashSetSupplier<T> implements Supplier<Set<T>> {

        @Override
        public Set<T> get() {
            return Sets.newHashSet();
        }
    }
}
