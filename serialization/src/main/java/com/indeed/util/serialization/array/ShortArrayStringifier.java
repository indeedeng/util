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

/** @author jplaisance */
public final class ShortArrayStringifier implements Stringifier<short[]> {
    private static final Logger log = LoggerFactory.getLogger(ShortArrayStringifier.class);

    @Override
    public String toString(short[] shorts) {
        return Arrays.toString(shorts);
    }

    @Override
    public short[] fromString(String str) {
        List<Short> shorts = Lists.newArrayList();
        EscapeAwareSplitter splitter =
                new EscapeAwareSplitter(
                        CharMatcher.whitespace().or(CharMatcher.is(',')),
                        EscapeAwareSplitter.NO_ESCAPE_LEXER_SUPPLIER);
        Iterator<String> split = splitter.split(str.substring(1, str.length() - 1));
        while (split.hasNext()) {
            shorts.add(Short.parseShort(split.next()));
        }
        short[] ret = new short[shorts.size()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = shorts.get(i);
        }
        return ret;
    }
}
