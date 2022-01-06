package com.indeed.util.serialization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jplaisance
 */
public final class StringStringifier implements Stringifier<String> {
    private static final Logger log = LoggerFactory.getLogger(StringStringifier.class);

    @Override
    public String toString(String s) {
        return s;
    }

    @Override
    public String fromString(String str) {
        return str;
    }
}
