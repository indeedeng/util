package com.indeed.util.serialization;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * @author jplaisance
 */
public final class DoubleStringifier implements Stringifier<Double> {
    private static final Logger log = LogManager.getLogger(DoubleStringifier.class);

    @Override
    public String toString(Double aDouble) {
        return aDouble.toString();
    }

    @Override
    public Double fromString(String str) {
        return Double.parseDouble(str);
    }
}
