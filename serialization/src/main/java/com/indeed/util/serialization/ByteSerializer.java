package com.indeed.util.serialization;

import org.apache.log4j.Logger;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * @author jplaisance
 */
public final class ByteSerializer implements Serializer<Byte> {

    private static final Logger log = Logger.getLogger(ByteSerializer.class);

    @Override
    public void write(final Byte val, final DataOutput out) throws IOException {
        out.writeByte(val);
    }

    @Override
    public Byte read(final DataInput in) throws IOException {
        return in.readByte();
    }
}
