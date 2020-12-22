package com.indeed.util.serialization;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * @author jplaisance
 */
public final class ByteSerializer implements Serializer<Byte> {

    private static final Logger log = LogManager.getLogger(ByteSerializer.class);

    @Override
    public void write(final Byte val, final DataOutput out) throws IOException {
        out.writeByte(val);
    }

    @Override
    public Byte read(final DataInput in) throws IOException {
        return in.readByte();
    }
}
