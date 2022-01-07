package com.indeed.util.serialization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author jplaisance */
public final class ByteStringifier implements Stringifier<Byte> {
    private static final Logger log = LoggerFactory.getLogger(ByteStringifier.class);

    @Override
    public String toString(Byte aByte) {
        return aByte.toString();
    }

    @Override
    public Byte fromString(String str) {
        return Byte.parseByte(str);
    }
}
