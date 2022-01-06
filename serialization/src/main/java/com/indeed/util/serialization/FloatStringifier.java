package com.indeed.util.serialization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jplaisance
 */
public final class FloatStringifier implements Stringifier<Float> {
    private static final Logger log = LoggerFactory.getLogger(FloatStringifier.class);

    @Override
    public String toString(Float aFloat) {
        return aFloat.toString();
    }

    @Override
    public Float fromString(String str) {
        return Float.valueOf(str);
    }
}
