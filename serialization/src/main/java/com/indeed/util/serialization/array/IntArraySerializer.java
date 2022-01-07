package com.indeed.util.serialization.array;

import com.indeed.util.serialization.LengthVIntSerializer;
import com.indeed.util.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/** @author jplaisance */
public final class IntArraySerializer implements Serializer<int[]> {
    private static final Logger log = LoggerFactory.getLogger(IntArraySerializer.class);

    private static final LengthVIntSerializer lengthSerializer = new LengthVIntSerializer();

    @Override
    public void write(int[] values, DataOutput out) throws IOException {
        lengthSerializer.write(values.length, out);
        for (int val : values) {
            out.writeInt(val);
        }
    }

    @Override
    public int[] read(DataInput in) throws IOException {
        final int length = lengthSerializer.read(in);
        final int[] values = new int[length];
        for (int i = 0; i < values.length; i++) {
            values[i] = in.readInt();
        }
        return values;
    }
}
