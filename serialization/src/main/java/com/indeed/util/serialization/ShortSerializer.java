package com.indeed.util.serialization;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * @author jplaisance
 */
public final class ShortSerializer implements Serializer<Short> {

    private static final Logger log = LogManager.getLogger(ShortSerializer.class);

    @Override
    public void write(final Short val, final DataOutput out) throws IOException {
        out.writeShort(val);
    }

    @Override
    public Short read(final DataInput in) throws IOException {
        return in.readShort();
    }
}
