package com.indeed.util.serialization.array;

import com.indeed.util.serialization.LengthVIntSerializer;
import com.indeed.util.serialization.Serializer;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * @author jplaisance
 */
public final class LongArraySerializer implements Serializer<long[]> {
    private static final Logger log = LogManager.getLogger(LongArraySerializer.class);

    private static final LengthVIntSerializer lengthSerializer = new LengthVIntSerializer();

    @Override
    public void write(long[] values, DataOutput out) throws IOException {
        lengthSerializer.write(values.length, out);
        for (long val : values) {
            out.writeLong(val);
        }
    }

    @Override
    public long[] read(DataInput in) throws IOException {
        final int length = lengthSerializer.read(in);
        final long[] values = new long[length];
        for (int i = 0; i < values.length; i++) {
            values[i] = in.readLong();
        }
        return values;
    }
}
