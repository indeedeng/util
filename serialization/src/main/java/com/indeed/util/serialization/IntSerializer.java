package com.indeed.util.serialization;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * @author jplaisance
 */
public final class IntSerializer implements Serializer<Integer> {

    private static final Logger log = LogManager.getLogger(IntSerializer.class);

    @Override
    public void write(Integer i, final DataOutput out) throws IOException {
        out.writeInt(i);
    }

    @Override
    public Integer read(final DataInput in) throws IOException {
        return in.readInt();
    }
}
