package com.indeed.util.serialization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author jplaisance */
public final class BooleanStringifier implements Stringifier<Boolean> {
    private static final Logger log = LoggerFactory.getLogger(BooleanStringifier.class);

    @Override
    public String toString(Boolean aBoolean) {
        return aBoolean.toString();
    }

    @Override
    public Boolean fromString(String str) {
        return Boolean.parseBoolean(str);
    }
}
