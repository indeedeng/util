package com.indeed.util.serialization.array;

import com.indeed.util.serialization.LengthVIntSerializer;
import com.indeed.util.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/** @author jplaisance */
public final class ByteArraySerializer implements Serializer<byte[]> {
    private static final Logger log = LoggerFactory.getLogger(ByteArraySerializer.class);

    private static final LengthVIntSerializer lengthSerializer = new LengthVIntSerializer();

    @Override
    public void write(byte[] values, DataOutput out) throws IOException {
        lengthSerializer.write(values.length, out);
        for (byte val : values) {
            out.writeByte(val);
        }
    }

    @Override
    public byte[] read(DataInput in) throws IOException {
        final int length = lengthSerializer.read(in);
        final byte[] values = new byte[length];
        for (int i = 0; i < values.length; i++) {
            values[i] = in.readByte();
        }
        return values;
    }
}
