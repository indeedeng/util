package com.indeed.util.core;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

public class NetUtilsTest {
    @Test
    public void testGetRawAddress() {
        assertArrayEquals(make(1, 1, 1, 1), NetUtils.getRawAddress("1.1.1.1"));
        assertArrayEquals(make(192, 168, 0, 1), NetUtils.getRawAddress("192.168.0.1"));
        assertArrayEquals(make(128, 127, 254, 255), NetUtils.getRawAddress("128.127.254.255"));
    }

    private byte[] make(int a, int b, int c, int d) {
        return new byte[] {(byte) a, (byte) b, (byte) c, (byte) d};
    }

    private void assertArrayEquals(byte[] a, byte[] b) {
        if (!Arrays.equals(a, b))
            fail(
                    "Arrays are not equal, expected: "
                            + Arrays.toString(a)
                            + ", actual: "
                            + Arrays.toString(b));
    }

    @Test
    public void testGetHostname() throws Exception {
        assertFalse(NetUtils.determineHostName("unknown").equals("unknown"));
        assertFalse(NetUtils.determineHostName("unknown").equals("unknown"));
        assertFalse(NetUtils.determineHostName("unknown").equals("unknown"));
    }

    @Test(expected = NullPointerException.class)
    public void testNullException() throws Exception {
        NetUtils.determineHostName(null);
    }
}
