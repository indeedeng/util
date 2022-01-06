package com.indeed.util.serialization.array;

import com.indeed.util.serialization.LengthVIntSerializer;
import com.indeed.util.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * @author jplaisance
 */
public final class BooleanArraySerializer implements Serializer<boolean[]> {
    private static final Logger log = LoggerFactory.getLogger(BooleanArraySerializer.class);

    private static final LengthVIntSerializer lengthSerializer = new LengthVIntSerializer();

    @Override
    public void write(boolean[] values, DataOutput out) throws IOException {
        lengthSerializer.write(values.length, out);
        for (boolean val : values) {
            out.writeBoolean(val);
        }
    }

    @Override
    public boolean[] read(DataInput in) throws IOException {
        final int length = lengthSerializer.read(in);
        final boolean[] values = new boolean[length];
        for (int i = 0; i < values.length; i++) {
            values[i] = in.readBoolean();
        }
        return values;
    }
}
