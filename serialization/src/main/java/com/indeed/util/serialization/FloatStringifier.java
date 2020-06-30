package com.indeed.util.serialization;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * @author jplaisance
 */
public final class FloatStringifier implements Stringifier<Float> {
    private static final Logger log = LogManager.getLogger(FloatStringifier.class);

    @Override
    public String toString(Float aFloat) {
        return aFloat.toString();
    }

    @Override
    public Float fromString(String str) {
        return Float.valueOf(str);
    }
}
