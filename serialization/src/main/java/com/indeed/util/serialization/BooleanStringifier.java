package com.indeed.util.serialization;

import org.apache.log4j.Logger;

/**
 * @author jplaisance
 */
public final class BooleanStringifier implements Stringifier<Boolean> {
    private static final Logger log = Logger.getLogger(BooleanStringifier.class);

    @Override
    public String toString(Boolean aBoolean) {
        return aBoolean.toString();
    }

    @Override
    public Boolean fromString(String str) {
        return Boolean.parseBoolean(str);
    }
}
