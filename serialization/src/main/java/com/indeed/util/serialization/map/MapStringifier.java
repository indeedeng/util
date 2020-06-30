package com.indeed.util.serialization.map;

import com.google.common.base.CharMatcher;
import com.google.common.base.Supplier;
import com.indeed.util.serialization.CollectionSuppliers;
import com.indeed.util.serialization.Stringifier;
import com.indeed.util.serialization.splitter.EscapeAwareSplitter;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.Iterator;
import java.util.Map;

/**
 * @author jplaisance
 */
public final class MapStringifier<K,V> implements Stringifier<Map<K, V>> {
    private static final Logger log = LogManager.getLogger(MapStringifier.class);

    private static final EscapeAwareSplitter splitter = new EscapeAwareSplitter(CharMatcher.whitespace().or(CharMatcher.anyOf(",=")), EscapeAwareSplitter.ESCAPE_JAVA_LEXER_SUPPLIER);

    public static <K,V> MapStringifier<K,V> hashMapStringifier(Stringifier<K> keyStringifier, Stringifier<V> valueStringifier) {
        return new MapStringifier<K, V>(new CollectionSuppliers.HashMapSupplier<K, V>(), keyStringifier, valueStringifier);
    }

    private final Supplier<? extends Map<K, V>> mapSupplier;
    private final Stringifier<K> keyStringifier;
    private final Stringifier<V> valueStringifier;

    public MapStringifier(Supplier<? extends Map<K,V>> mapSupplier, Stringifier<K> keyStringifier, Stringifier<V> valueStringifier) {
        this.mapSupplier = mapSupplier;
        this.keyStringifier = keyStringifier;
        this.valueStringifier = valueStringifier;
    }

    @Override
    public String toString(Map<K, V> map) {
        StringBuilder builder = new StringBuilder();
        builder.append('{');
        for (Map.Entry<K, V> entry : map.entrySet()) {
            builder.append('"');
            builder.append(StringEscapeUtils.escapeJava(keyStringifier.toString(entry.getKey())));
            builder.append("\"=\"");
            builder.append(StringEscapeUtils.escapeJava(valueStringifier.toString(entry.getValue())));
            builder.append("\", ");
        }
        if (!map.isEmpty()) {
            builder.setLength(builder.length()-2);
        }
        builder.append('}');
        return builder.toString();
    }

    @Override
    public Map<K, V> fromString(String str) {
        Map<K,V> ret = mapSupplier.get();
        Iterator<String> split = splitter.split(str.substring(1, str.length()-1));
        while (split.hasNext()) {
            K key = keyStringifier.fromString(split.next());
            if (!split.hasNext()) throw new IllegalArgumentException();
            V val = valueStringifier.fromString(split.next());
            ret.put(key, val);
        }
        return ret;
    }
}
