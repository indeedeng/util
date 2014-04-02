package com.indeed.util.serialization;

import org.apache.log4j.Logger;

/**
 * @author jplaisance
 */
public final class DoubleStringifier implements Stringifier<Double> {
    private static final Logger log = Logger.getLogger(DoubleStringifier.class);

    @Override
    public String toString(Double aDouble) {
        return aDouble.toString();
    }

    @Override
    public Double fromString(String str) {
        return Double.parseDouble(str);
    }
}
