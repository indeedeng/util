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
public final class CharArrayStringifier implements Stringifier<char[]> {
    private static final Logger log = Logger.getLogger(CharArrayStringifier.class);

    @Override
    public String toString(char[] chars) {
        return Arrays.toString(chars);
    }

    @Override
    public char[] fromString(String str) {
        List<Character> chars = Lists.newArrayList();
        EscapeAwareSplitter splitter = new EscapeAwareSplitter(CharMatcher.WHITESPACE.or(CharMatcher.is(',')), EscapeAwareSplitter.NO_ESCAPE_LEXER_SUPPLIER);
        Iterator<String> split = splitter.split(str.substring(1, str.length()-1));
        while (split.hasNext()) {
            chars.add(split.next().charAt(0));
        }
        char[] ret = new char[chars.size()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = chars.get(i);
        }
        return ret;
    }
}
