package com.indeed.util.core;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author jack@indeed.com (Jack Humphrey)
 */
public class ReleaseVersionTest {
    public static void assertVersionLessThan(final String lhs, final String rhs) {
        assertEquals(-1, ReleaseVersion.fromString(lhs).compareTo(ReleaseVersion.fromString(rhs)));
    }

    public static void assertVersionGreaterThan(final String lhs, final String rhs) {
        assertEquals(1, ReleaseVersion.fromString(lhs).compareTo(ReleaseVersion.fromString(rhs)));
    }

    public static void assertVersionEquals(final String lhs, final String rhs) {
        assertEquals(0, ReleaseVersion.fromString(lhs).compareTo(ReleaseVersion.fromString(rhs)));
    }

    @Test
    public void testVersionComparison() {
        assertVersionLessThan("3.1.1.0", "3.1.1.1");
        assertVersionLessThan("3.1.1.0", "3.2.1.1");
        assertVersionEquals("3.1.1.x", "3.1.1.1");
        assertVersionEquals("3.1.x", "3.1.1.1");
        assertVersionEquals("3.x", "3.1.1.1");
        assertVersionEquals("4.0.x", "4.0.1.0");
        assertVersionLessThan("4.0.0.0", "4.1.0.0");
        assertVersionLessThan("4.0.0.0", "5.0.0.0");
        assertVersionEquals("4.x", "4.0.0.1");

        assertVersionGreaterThan("3.1.1.1", "3.1.1.0");
        assertVersionGreaterThan("3.2.1.0", "3.1.1.1");
        assertVersionGreaterThan("3.2.1.0", "3.1.x");
        assertVersionGreaterThan("3.1.1.0", "3.1.0.x");
        assertVersionEquals("3.1.1.1", "3.1.1.x");
        assertVersionEquals("3.1.1.1", "3.1.x");
        assertVersionEquals("3.1.1.1", "3.x");
        assertVersionEquals("4.0.1.0", "4.0.x");
        assertVersionGreaterThan("4.1.0.0", "4.0.0.0");
        assertVersionGreaterThan("5.0.0.0", "4.0.0.0");
        assertVersionEquals("4.0.0.1", "4.x");

        assertVersionEquals("0.x", "0.0.0.0");
        assertVersionEquals("1.524.x", "1.524.0.0");
        assertVersionEquals("1.x", "1.0.0.0");
        assertVersionEquals("1.0.0.x", "1.0.0.0");
        assertVersionEquals("1.0.x", "1.0.0.0");
        assertVersionEquals("1.x", "1.0.0.0");
        assertVersionEquals("1.0.x", "1.0.0.0");
        assertVersionEquals("1.0.0.x", "1.0.0.0");
        assertVersionEquals("1.x", "1.0.0.0");
        assertVersionEquals("1.0.x", "1.0.0.0");
        assertVersionEquals("1.0.0.0", "1.x");
        assertVersionEquals("32767.32767.32767.32767", "32767.32767.32767.32767");

        // more than 4 parts discarded
        assertVersionEquals("1.1.1.1", "1.1.1.1.999");

        assertVersionGreaterThan("32767.65535.65535.65535", "32767.65535.65535.65534");

        assertVersionEquals("1.1.x", "1.1.1.x");
        assertVersionGreaterThan("1.1.x", "1.0.0.x");
        assertVersionGreaterThan("1.2.x", "1.1.x");
        assertVersionLessThan("1.0.0.x", "1.1.x");
    }

    public static void assertIllegalArgumentException(String version) {
        try {
            ReleaseVersion.fromString(version);
            fail("expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void testInvalidVersions() {
        assertIllegalArgumentException("1");
        assertIllegalArgumentException("1.1");
        assertIllegalArgumentException("1.1.1");
        assertIllegalArgumentException("32768.0.0.0");
        assertIllegalArgumentException("32767.65536.x");
        assertIllegalArgumentException("32767.65535.65536.x");
        assertIllegalArgumentException("32767.65535.65535.65536");
        assertIllegalArgumentException("-1.x");
        assertIllegalArgumentException("1.-206.x");
        assertIllegalArgumentException("1.1.-29348.0");
        assertIllegalArgumentException("1.1.1.-29348.0");
        assertIllegalArgumentException("1..1.x");
        assertIllegalArgumentException("x"); // TODO: should this be supported? ("match all versions")
        assertIllegalArgumentException("");
    }

    @Test
    public void testFromStringSafely() {

        final ReleaseVersion expectedVersion = ReleaseVersion.fromString("1.0.0.0");
        assertEquals(expectedVersion, ReleaseVersion.fromStringSafely("1.0.rc1", null));
        assertEquals(expectedVersion, ReleaseVersion.fromStringSafely("1.-206.x", null));
        assertEquals(expectedVersion, ReleaseVersion.fromStringSafely("1.0.0.65536", null));

        final ReleaseVersion defaultVersion = ReleaseVersion.newBuilder().build();
        assertEquals(defaultVersion, ReleaseVersion.fromStringSafely("32768.x", defaultVersion));
        assertEquals(defaultVersion, ReleaseVersion.fromStringSafely("x", defaultVersion));
        assertEquals(defaultVersion, ReleaseVersion.fromStringSafely("", defaultVersion));
    }

    @Test
    public void testVersionToString() {
        assertEquals("1.x", ReleaseVersion.fromString("1.x").toString());
        assertEquals("1.1.x", ReleaseVersion.fromString("1.1.x").toString());
        assertEquals("1.1.1.x", ReleaseVersion.fromString("1.1.1.x").toString());
        assertEquals("1.1.1.1", ReleaseVersion.fromString("1.1.1.1").toString());
        assertEquals("1.1.1.1", ReleaseVersion.fromString("1.1.1.1.x").toString());
        assertEquals("1.1.1.1", ReleaseVersion.fromString("1.1.1.1.x.x").toString());
        assertEquals("1.1.1.1", ReleaseVersion.fromString("1.1.1.1.x.x.itsallgood").toString());
        assertEquals("32767.65535.65535.65535", ReleaseVersion.fromString("32767.65535.65535.65535").toString());
    }
}
