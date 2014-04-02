package com.indeed.util.serialization.array;

import com.indeed.util.serialization.LengthVIntSerializer;
import com.indeed.util.serialization.Serializer;
import org.apache.log4j.Logger;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * @author jplaisance
 */
public final class FloatArraySerializer implements Serializer<float[]> {
    private static final Logger log = Logger.getLogger(FloatArraySerializer.class);

    private static final LengthVIntSerializer lengthSerializer = new LengthVIntSerializer();

    @Override
    public void write(float[] values, DataOutput out) throws IOException {
        lengthSerializer.write(values.length, out);
        for (float val : values) {
            out.writeFloat(val);
        }
    }

    @Override
    public float[] read(DataInput in) throws IOException {
        final int length = lengthSerializer.read(in);
        final float[] values = new float[length];
        for (int i = 0; i < values.length; i++) {
            values[i] = in.readFloat();
        }
        return values;
    }
}
