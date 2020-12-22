package com.indeed.util.serialization;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * @author jplaisance
 */
public final class CharSerializer implements Serializer<Character> {

    private static final Logger log = LogManager.getLogger(CharSerializer.class);

    @Override
    public void write(final Character val, final DataOutput out) throws IOException {
        out.writeChar(val);
    }

    @Override
    public Character read(final DataInput in) throws IOException {
        return in.readChar();
    }
}
