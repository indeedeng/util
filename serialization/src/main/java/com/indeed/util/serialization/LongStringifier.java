package com.indeed.util.serialization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author jplaisance */
public final class LongStringifier implements Stringifier<Long> {

    private static final Logger log = LoggerFactory.getLogger(LongStringifier.class);

    @Override
    public String toString(final Long l) {
        return String.valueOf(l);
    }

    @Override
    public Long fromString(final String str) {
        return Long.valueOf(str);
    }
}
