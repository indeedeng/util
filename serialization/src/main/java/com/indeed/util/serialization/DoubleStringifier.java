package com.indeed.util.serialization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author jplaisance */
public final class DoubleStringifier implements Stringifier<Double> {
    private static final Logger log = LoggerFactory.getLogger(DoubleStringifier.class);

    @Override
    public String toString(Double aDouble) {
        return aDouble.toString();
    }

    @Override
    public Double fromString(String str) {
        return Double.parseDouble(str);
    }
}
