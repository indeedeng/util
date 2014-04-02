package com.indeed.util.serialization;

import org.apache.log4j.Logger;

/**
 * @author jplaisance
 */
public final class ShortStringifier implements Stringifier<Short> {
    private static final Logger log = Logger.getLogger(ShortStringifier.class);

    @Override
    public String toString(Short aShort) {
        return aShort.toString();
    }

    @Override
    public Short fromString(String str) {
        return Short.valueOf(str);
    }
}
