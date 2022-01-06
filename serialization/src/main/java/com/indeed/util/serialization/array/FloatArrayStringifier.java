package com.indeed.util.serialization.array;

import com.google.common.base.CharMatcher;
import com.google.common.collect.Lists;
import com.indeed.util.serialization.Stringifier;
import com.indeed.util.serialization.splitter.EscapeAwareSplitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author jplaisance
 */
public final class FloatArrayStringifier implements Stringifier<float[]> {
    private static final Logger log = LoggerFactory.getLogger(FloatArrayStringifier.class);

    @Override
    public String toString(float[] floats) {
        return Arrays.toString(floats);
    }

    @Override
    public float[] fromString(String str) {
        List<Float> floats = Lists.newArrayList();
        EscapeAwareSplitter splitter = new EscapeAwareSplitter(CharMatcher.whitespace().or(CharMatcher.is(',')), EscapeAwareSplitter.NO_ESCAPE_LEXER_SUPPLIER);
        Iterator<String> split = splitter.split(str.substring(1, str.length()-1));
        while (split.hasNext()) {
            floats.add(Float.parseFloat(split.next()));
        }
        float[] ret = new float[floats.size()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = floats.get(i);
        }
        return ret;
    }
}
