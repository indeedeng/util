package com.indeed.util.mmap;

import com.google.common.io.ByteStreams;
import com.google.common.io.InputSupplier;
import junit.framework.TestCase;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

/**
 * @author jplaisance
 */
public final class TestZeroCopyOutputStream extends TestCase {
    private static final Logger log = Logger.getLogger(TestZeroCopyOutputStream.class);

    public void testStuff() throws IOException {
        final byte[] bytes = new byte[1024*1024*4];
        final ZeroCopyOutputStream out = new ZeroCopyOutputStream();
        final Random r = new Random(0);
        r.nextBytes(bytes);
        for (int i = 0; i < bytes.length; i+= 1024) {
            assertTrue(ByteStreams.equal(new InputSupplier<InputStream>() {
                public InputStream getInput() throws IOException {
                    return out.getInputStream();
                }
            }, ByteStreams.newInputStreamSupplier(bytes, 0, i)));
            out.write(bytes, i, 1024);
        }
        assertTrue(ByteStreams.equal(new InputSupplier<InputStream>() {
            public InputStream getInput() throws IOException {
                return out.getInputStream();
            }
        }, ByteStreams.newInputStreamSupplier(bytes)));
    }


}
