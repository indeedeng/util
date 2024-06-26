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
public final class LongArrayStringifier implements Stringifier<long[]> {
    private static final Logger log = LoggerFactory.getLogger(LongArrayStringifier.class);

    @Override
    public String toString(long[] longs) {
        return Arrays.toString(longs);
    }

    @Override
    public long[] fromString(String str) {
        List<Long> longs = Lists.newArrayList();
        EscapeAwareSplitter splitter =
                new EscapeAwareSplitter(
                        CharMatcher.whitespace().or(CharMatcher.is(',')),
                        EscapeAwareSplitter.NO_ESCAPE_LEXER_SUPPLIER);
        Iterator<String> split = splitter.split(str.substring(1, str.length() - 1));
        while (split.hasNext()) {
            longs.add(Long.parseLong(split.next()));
        }
        long[] ret = new long[longs.size()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = longs.get(i);
        }
        return ret;
    }
}
