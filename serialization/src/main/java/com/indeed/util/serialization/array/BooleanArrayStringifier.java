package com.indeed.util.serialization.array;

import com.google.common.base.CharMatcher;
import com.google.common.collect.Lists;
import com.indeed.util.serialization.Stringifier;
import com.indeed.util.serialization.splitter.EscapeAwareSplitter;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author jplaisance
 */
public final class BooleanArrayStringifier implements Stringifier<boolean[]>{
    private static final Logger log = LogManager.getLogger(BooleanArrayStringifier.class);

    @Override
    public String toString(boolean[] booleans) {
        return Arrays.toString(booleans);
    }

    @Override
    public boolean[] fromString(String str) {
        List<Boolean> booleans = Lists.newArrayList();
        EscapeAwareSplitter splitter = new EscapeAwareSplitter(CharMatcher.whitespace().or(CharMatcher.is(',')), EscapeAwareSplitter.NO_ESCAPE_LEXER_SUPPLIER);
        Iterator<String> split = splitter.split(str.substring(1, str.length()-1));
        while (split.hasNext()) {
            booleans.add(Boolean.parseBoolean(split.next()));
        }
        boolean[] ret = new boolean[booleans.size()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = booleans.get(i);
        }
        return ret;
    }
}
