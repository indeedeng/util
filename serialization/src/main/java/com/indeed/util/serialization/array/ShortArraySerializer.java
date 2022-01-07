package com.indeed.util.serialization.array;

import com.indeed.util.serialization.LengthVIntSerializer;
import com.indeed.util.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/** @author jplaisance */
public final class ShortArraySerializer implements Serializer<short[]> {
    private static final Logger log = LoggerFactory.getLogger(ShortArraySerializer.class);

    private static final LengthVIntSerializer lengthSerializer = new LengthVIntSerializer();

    @Override
    public void write(short[] values, DataOutput out) throws IOException {
        lengthSerializer.write(values.length, out);
        for (short val : values) {
            out.writeShort(val);
        }
    }

    @Override
    public short[] read(DataInput in) throws IOException {
        final int length = lengthSerializer.read(in);
        final short[] values = new short[length];
        for (int i = 0; i < values.length; i++) {
            values[i] = in.readShort();
        }
        return values;
    }
}
