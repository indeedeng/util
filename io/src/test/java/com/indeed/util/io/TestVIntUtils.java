package com.indeed.util.io;

import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @author jplaisance
 */
public final class TestVIntUtils extends TestCase {

    private static final Logger log = LoggerFactory.getLogger(TestVIntUtils.class);

    int[] ints = new int[]{1, -1, 0, Integer.MAX_VALUE, Integer.MIN_VALUE, 63, -64, 64, -65};

    public void testSVInt() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (int i : ints) {
            VIntUtils.writeSVInt(out, i);
        }
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        for (int i : ints) {
            assertTrue(i == VIntUtils.readSVInt(in));
        }
        assertTrue(out.size() == 19);
    }
}
