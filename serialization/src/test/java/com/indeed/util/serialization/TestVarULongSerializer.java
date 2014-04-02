package com.indeed.util.serialization;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import junit.framework.TestCase;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * @author jplaisance
 */
public final class TestVarULongSerializer extends TestCase {
    private static final Logger log = Logger.getLogger(TestVarULongSerializer.class);

    public void testStuff() throws IOException {
        testLong(0);
        testLong(1);
        testLong(Integer.MAX_VALUE);
        testLong(((long)Integer.MAX_VALUE)+1);
        testLong(Long.MAX_VALUE-1);
        testLong(Long.MAX_VALUE);
        for (int i = 0; i < 63; i++) {
            testLong(1L<<i);
        }
        for (int i = 1; i < 64; i++) {
            testLong((1L<<i)-1);
        }
        for (int i = 0; i < 31; i++) {
            testInt(1<<i);
        }
        for (int i = 1; i < 32; i++) {
            testInt((1<<i)-1);
        }
    }

    public void testLong(long l) throws IOException {
        final VarULongSerializer serializer = new VarULongSerializer();
        final ByteArrayDataOutput out = ByteStreams.newDataOutput();
        serializer.write(l, out);
        assertEquals(l, serializer.read(ByteStreams.newDataInput(out.toByteArray())).longValue());
    }

    public void testInt(int i) throws IOException {
        final VarULongSerializer serializer = new VarULongSerializer();
        final ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeInt(i);
        assertEquals(i, serializer.read(ByteStreams.newDataInput(out.toByteArray())).longValue());
    }
}
