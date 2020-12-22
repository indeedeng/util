package com.indeed.util.serialization;

import com.google.common.base.Charsets;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * @author jplaisance
 */
public final class StringSerializer implements Serializer<String> {

    private static final Logger log = LogManager.getLogger(StringSerializer.class);

    private static final LengthVIntSerializer lengthSerializer = new LengthVIntSerializer();

    @Override
    public void write(final String s, final DataOutput out) throws IOException {
        byte[] bytes = s.getBytes(Charsets.UTF_8);
        lengthSerializer.write(bytes.length, out);
        out.write(bytes);
    }

    @Override
    public String read(final DataInput in) throws IOException {
        int length = lengthSerializer.read(in);
        byte[] bytes = new byte[length];
        in.readFully(bytes);
        return new String(bytes, Charsets.UTF_8);
    }
}
