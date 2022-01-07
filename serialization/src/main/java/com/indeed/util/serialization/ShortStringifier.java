package com.indeed.util.serialization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author jplaisance */
public final class ShortStringifier implements Stringifier<Short> {
    private static final Logger log = LoggerFactory.getLogger(ShortStringifier.class);

    @Override
    public String toString(Short aShort) {
        return aShort.toString();
    }

    @Override
    public Short fromString(String str) {
        return Short.valueOf(str);
    }
}
