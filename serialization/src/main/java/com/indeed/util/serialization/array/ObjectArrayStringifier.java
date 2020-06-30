package com.indeed.util.serialization.array;

import com.indeed.util.serialization.Stringifier;
import com.indeed.util.serialization.list.ListStringifier;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

/**
* @author jplaisance
*/
public final class ObjectArrayStringifier<E> implements Stringifier<E[]> {
    private static final Logger log = LogManager.getLogger(ObjectArrayStringifier.class);

    private final ListStringifier<E> listStringifier;
    private final Class<E> type;


    public ObjectArrayStringifier(Stringifier<E> stringifier, Class<E> type) {
        this.listStringifier = ListStringifier.arrayListStringifier(stringifier);
        this.type = type;
    }

    @Override
    public String toString(E[] objects) {
        return listStringifier.toString(Arrays.asList(objects));
    }

    @Override
    public E[] fromString(String str) {
        List<E> values = listStringifier.fromString(str);
        return values.toArray((E[]) Array.newInstance(type, values.size()));
    }
}
