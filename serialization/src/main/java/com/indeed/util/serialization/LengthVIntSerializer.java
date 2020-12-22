package com.indeed.util.serialization;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Serializer that writes 0-254 as one byte. Any value greater than 254 or less than 0 is written as 5 bytes.
 *
 * @author jplaisance
 */
public final class LengthVIntSerializer implements Serializer<Integer> {
    private static final Logger log = LogManager.getLogger(LengthVIntSerializer.class);

    @Override
    public void write(Integer i, DataOutput out) throws IOException {
        if (i < 0xFF) {
            out.writeByte(i);
        } else {
            out.writeByte(0xFF);
            out.writeInt(i);
        }
    }

    @Override
    public Integer read(DataInput in) throws IOException {
        final int firstByte = in.readByte()&0xFF;
        if (firstByte != 0xFF) {
            return firstByte;
        }
        return in.readInt();
    }
}
