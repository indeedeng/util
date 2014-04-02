package com.indeed.util.serialization.array;

import com.google.common.base.CharMatcher;
import com.google.common.collect.Lists;
import com.indeed.util.serialization.Stringifier;
import com.indeed.util.serialization.splitter.EscapeAwareSplitter;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author jplaisance
 */
public final class IntArrayStringifier implements Stringifier<int[]> {
    private static final Logger log = Logger.getLogger(IntArrayStringifier.class);

    @Override
    public String toString(int[] ints) {
        return Arrays.toString(ints);
    }

    @Override
    public int[] fromString(String str) {
        List<Integer> ints = Lists.newArrayList();
        EscapeAwareSplitter splitter = new EscapeAwareSplitter(CharMatcher.WHITESPACE.or(CharMatcher.is(',')), EscapeAwareSplitter.NO_ESCAPE_LEXER_SUPPLIER);
        Iterator<String> split = splitter.split(str.substring(1, str.length()-1));
        while (split.hasNext()) {
            ints.add(Integer.parseInt(split.next()));
        }
        int[] ret = new int[ints.size()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = ints.get(i);
        }
        return ret;
    }
}
