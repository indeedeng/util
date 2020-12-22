package com.indeed.util.serialization;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * @author jplaisance
 */
public final class ShortStringifier implements Stringifier<Short> {
    private static final Logger log = LogManager.getLogger(ShortStringifier.class);

    @Override
    public String toString(Short aShort) {
        return aShort.toString();
    }

    @Override
    public Short fromString(String str) {
        return Short.valueOf(str);
    }
}
