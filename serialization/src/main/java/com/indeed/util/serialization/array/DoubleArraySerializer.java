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
public final class DoubleArraySerializer implements Serializer<double[]> {
    private static final Logger log = LogManager.getLogger(DoubleArraySerializer.class);

    private static final LengthVIntSerializer lengthSerializer = new LengthVIntSerializer();

    @Override
    public void write(double[] values, DataOutput out) throws IOException {
        lengthSerializer.write(values.length, out);
        for (double val : values) {
            out.writeDouble(val);
        }
    }

    @Override
    public double[] read(DataInput in) throws IOException {
        final int length = lengthSerializer.read(in);
        final double[] values = new double[length];
        for (int i = 0; i < values.length; i++) {
            values[i] = in.readDouble();
        }
        return values;
    }
}
