package com.indeed.util.serialization;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * @author jplaisance
 */
public final class BooleanStringifier implements Stringifier<Boolean> {
    private static final Logger log = LogManager.getLogger(BooleanStringifier.class);

    @Override
    public String toString(Boolean aBoolean) {
        return aBoolean.toString();
    }

    @Override
    public Boolean fromString(String str) {
        return Boolean.parseBoolean(str);
    }
}
