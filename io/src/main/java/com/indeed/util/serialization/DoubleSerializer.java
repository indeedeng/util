package com.indeed.util.serialization;

import org.apache.log4j.Logger;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * @author jplaisance
 */
public final class DoubleSerializer implements Serializer<Double> {

    private static final Logger log = Logger.getLogger(DoubleSerializer.class);

    @Override
    public void write(final Double val, final DataOutput out) throws IOException {
        out.writeDouble(val);
    }

    @Override
    public Double read(final DataInput in) throws IOException {
        return in.readDouble();
    }
}
