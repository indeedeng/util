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
public final class DoubleArrayStringifier implements Stringifier<double[]> {
    private static final Logger log = LoggerFactory.getLogger(DoubleArrayStringifier.class);

    @Override
    public String toString(double[] doubles) {
        return Arrays.toString(doubles);
    }

    @Override
    public double[] fromString(String str) {
        List<Double> doubles = Lists.newArrayList();
        EscapeAwareSplitter splitter =
                new EscapeAwareSplitter(
                        CharMatcher.whitespace().or(CharMatcher.is(',')),
                        EscapeAwareSplitter.NO_ESCAPE_LEXER_SUPPLIER);
        Iterator<String> split = splitter.split(str.substring(1, str.length() - 1));
        while (split.hasNext()) {
            doubles.add(Double.parseDouble(split.next()));
        }
        double[] ret = new double[doubles.size()];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = doubles.get(i);
        }
        return ret;
    }
}
