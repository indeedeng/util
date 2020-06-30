package com.indeed.util.serialization;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * @author jplaisance
 */
public final class ByteStringifier implements Stringifier<Byte> {
    private static final Logger log = LogManager.getLogger(ByteStringifier.class);

    @Override
    public String toString(Byte aByte) {
        return aByte.toString();
    }

    @Override
    public Byte fromString(String str) {
        return Byte.parseByte(str);
    }
}
