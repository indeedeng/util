package com.indeed.util.serialization;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * @author jplaisance
 */
public final class BooleanSerializer implements Serializer<Boolean> {

    private static final Logger log = LogManager.getLogger(BooleanSerializer.class);

    @Override
    public void write(final Boolean val, final DataOutput out) throws IOException {
        out.writeBoolean(val);
    }

    @Override
    public Boolean read(final DataInput in) throws IOException {
        return in.readBoolean();
    }
}
