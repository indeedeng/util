package com.indeed.util.serialization;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * @author jplaisance
 */
public final class StringStringifier implements Stringifier<String> {
    private static final Logger log = LogManager.getLogger(StringStringifier.class);

    @Override
    public String toString(String s) {
        return s;
    }

    @Override
    public String fromString(String str) {
        return str;
    }
}
