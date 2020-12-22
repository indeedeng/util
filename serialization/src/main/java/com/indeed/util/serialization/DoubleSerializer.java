package com.indeed.util.serialization;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * @author jplaisance
 */
public final class DoubleSerializer implements Serializer<Double> {

    private static final Logger log = LogManager.getLogger(DoubleSerializer.class);

    @Override
    public void write(final Double val, final DataOutput out) throws IOException {
        out.writeDouble(val);
    }

    @Override
    public Double read(final DataInput in) throws IOException {
        return in.readDouble();
    }
}
