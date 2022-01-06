package com.indeed.util.serialization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * @author jplaisance
 */
public final class VarULongSerializer implements Serializer<Long> {
    private static final Logger log = LoggerFactory.getLogger(VarULongSerializer.class);

    private static final long MAX_31 = 0x7FFFFFFF;
    public static final int HIGH_BIT = 1 << 31;
    public long INT_MASK = 0xFFFFFFFFL;

    public void write(final Long l, final DataOutput out) throws IOException {
        if (l <= MAX_31) {
            out.writeInt(l.intValue());
        } else {
            out.writeInt(((int)(l>>>32))|HIGH_BIT);
            out.writeInt(l.intValue());
        }
    }

    public Long read(final DataInput in) throws IOException {
        final int first = in.readInt();
        if (first >= 0) {
            return (long)first;
        }
        return ((first&MAX_31)<<32L)|(in.readInt()&INT_MASK);
    }
}
