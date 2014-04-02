package com.indeed.util.serialization;

import org.apache.log4j.Logger;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * @author jplaisance
 */
public final class LongSerializer implements Serializer<Long> {

    private static final Logger log = Logger.getLogger(LongSerializer.class);

    @Override
    public void write(final Long l, final DataOutput out) throws IOException {
        out.writeLong(l);
    }

    @Override
    public Long read(final DataInput in) throws IOException {
        return in.readLong();
    }
}
