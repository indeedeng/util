package com.indeed.util.mmap;

import com.google.common.io.ByteStreams;
import junit.framework.TestCase;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.assertArrayEquals;

/** @author jplaisance */
public final class TestZeroCopyOutputStream extends TestCase {
    public void testStuff() throws IOException {
        final byte[] bytes = new byte[1024 * 1024 * 4];
        final ZeroCopyOutputStream out = new ZeroCopyOutputStream();
        final Random r = new Random(0);
        r.nextBytes(bytes);
        for (int i = 0; i < bytes.length; i += 1024) {
            // too slow if comparing for every block written, test every N
            if ((i % (1024 * 256)) == 0) {
                assertArrayEquals(
                        ByteStreams.toByteArray(out.getInputStream()), Arrays.copyOf(bytes, i));
            }
            out.write(bytes, i, 1024);
        }
        assertArrayEquals(ByteStreams.toByteArray(out.getInputStream()), bytes);
        out.close();
    }
}
