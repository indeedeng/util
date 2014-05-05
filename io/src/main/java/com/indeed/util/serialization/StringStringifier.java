package com.indeed.util.serialization;

import org.apache.log4j.Logger;

/**
 * @author jplaisance
 */
public final class StringStringifier implements Stringifier<String> {
    private static final Logger log = Logger.getLogger(StringStringifier.class);

    @Override
    public String toString(String s) {
        return s;
    }

    @Override
    public String fromString(String str) {
        return str;
    }
}
