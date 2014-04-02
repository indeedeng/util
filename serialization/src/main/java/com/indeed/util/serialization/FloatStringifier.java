package com.indeed.util.serialization;

import org.apache.log4j.Logger;

/**
 * @author jplaisance
 */
public final class FloatStringifier implements Stringifier<Float> {
    private static final Logger log = Logger.getLogger(FloatStringifier.class);

    @Override
    public String toString(Float aFloat) {
        return aFloat.toString();
    }

    @Override
    public Float fromString(String str) {
        return Float.valueOf(str);
    }
}
