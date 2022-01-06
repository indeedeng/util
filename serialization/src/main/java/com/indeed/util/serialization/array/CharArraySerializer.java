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
public final class CharArraySerializer implements Serializer<char[]> {
    private static final Logger log = LoggerFactory.getLogger(CharArraySerializer.class);
    
    private static final LengthVIntSerializer lengthSerializer = new LengthVIntSerializer();

    @Override
    public void write(char[] values, DataOutput out) throws IOException {
        lengthSerializer.write(values.length, out);
        for (char val : values) {
            out.writeChar(val);
        }
    }

    @Override
    public char[] read(DataInput in) throws IOException {
        final int length = lengthSerializer.read(in);
        final char[] values = new char[length];
        for (int i = 0; i < values.length; i++) {
            values[i] = in.readChar();
        }
        return values;
    }
}
