package com.indeed.util.urlparsing;

import com.google.common.base.Preconditions;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Random;

import static junit.framework.Assert.assertEquals;

/** @author: preetha */
public class TestParseUtils {

    private static final int NUM_TRIALS = 50000;

    private static final long SEED = 21906731123608L;

    private final Random random = new Random(SEED);

    @Test
    public void testParseInt() {
        for (int k = 0; k < NUM_TRIALS; k++) {
            testIntParser(random, true);
            testIntParser(random, false);
        }
    }

    @Test
    public void testParseLong() {
        for (int k = 0; k < NUM_TRIALS; k++) {
            testLongParser(random);
        }
    }

    @Test
    public void testParseFloat() {
        for (int k = 0; k < NUM_TRIALS; k++) {
            testFloatParser(random, true);
            testFloatParser(random, false);
        }
    }

    @Test
    public void testScientificNotation() {
        String expFloat = "123e-2";
        assertEquals(1.23f, ParseUtils.parseFloat(expFloat, 0, expFloat.length()));
        assertEquals(0.23f, ParseUtils.parseFloat(expFloat, 1, expFloat.length()));
        assertEquals(0.03f, ParseUtils.parseFloat(expFloat, 2, expFloat.length()));
        assertEquals(0f, ParseUtils.parseFloat(expFloat, 3, expFloat.length()));
        assertEquals(123f, ParseUtils.parseFloat(expFloat, 0, expFloat.length() - 1));

        expFloat = "123e10";
        assertEquals(1230000000000f, ParseUtils.parseFloat(expFloat, 0, expFloat.length()));
        assertEquals(230000000000f, ParseUtils.parseFloat(expFloat, 1, expFloat.length()));
        assertEquals(30000000000f, ParseUtils.parseFloat(expFloat, 2, expFloat.length()));
        assertEquals(0f, ParseUtils.parseFloat(expFloat, 3, expFloat.length()));
        assertEquals(1230f, ParseUtils.parseFloat(expFloat, 0, expFloat.length() - 1));
        assertEquals(230f, ParseUtils.parseFloat(expFloat, 1, expFloat.length() - 1));
        assertEquals(30f, ParseUtils.parseFloat(expFloat, 2, expFloat.length() - 1));
        assertEquals(0f, ParseUtils.parseFloat(expFloat, 3, expFloat.length() - 1));
    }

    @Test
    public void testNan() {
        assertEquals(Float.NaN, ParseUtils.parseFloat("NaN", 0, 3));
        assertEquals(Float.POSITIVE_INFINITY, ParseUtils.parseFloat("Infinity", 0, 8));
        assertEquals(Float.NEGATIVE_INFINITY, ParseUtils.parseFloat("-Infinity", 0, 9));
    }

    @Test(expected = NumberFormatException.class)
    public void testParseBadAlphaInput() {
        ParseUtils.parseInt("abc", 0, 3);
    }

    @Test(expected = NumberFormatException.class)
    public void testParseBadAlphaNumericInput() {
        ParseUtils.parseUnsignedInt("-a123", 0, 4);
    }

    @Test(expected = NumberFormatException.class)
    public void testParseBadNaNInput() {
        ParseUtils.parseUnsignedInt("-Nana", 0, 4);
    }

    @Test(expected = NumberFormatException.class)
    public void testParseBadInfinityInput() {
        ParseUtils.parseUnsignedInt("Inf", 0, 3);
    }

    @Test
    public void testUrlDecode() {
        decodesTo("%61%62%63%64%26%26%2f%2F%65%66%67%68", "abcd&&//efgh");
        decodesTo("%61%62%63%64%26%26%2f%2F%65%66%67%6", "abcd&&//efg%6");
        decodesTo("%61%62%63%64%26%26%2f%2F%65%66%67%", "abcd&&//efg%");
        decodesTo("%61%62%63%64%26%26%2f%2F%65%66%67%+", "abcd&&//efg% ");
        decodesTo("%61%62%63%64%26%26%2f%2F%65%66%67%%68", "abcd&&//efg%h");
        decodesTo("%61%62%63%64%26%26++%2f%2F%65%66%67%%68", "abcd&&  //efg%h");
        decodesTo("%61%62%63%64%26%26+%2b+%2f%2F%65%66%67%%68", "abcd&& + //efg%h");
        decodesTo("+%61%62%63%64%26%26%2f%2F%65%66%67%%68", " abcd&&//efg%h");
        decodesTo("+zimmerm%C3%61dchen", " zimmerm%C3adchen");
        decodesTo("+zimmerm%C3%z1dchen", " zimmerm%C3%z1dchen");
        decodesTo("+zimmerm%C3%A4dchen", " zimmermädchen");
        decodesTo("Technologist+%E2%80%93+Full+Time", "Technologist – Full Time");
    }

    @Test
    public void testMixedMultiByteUrlDecode() throws UnsupportedEncodingException {
        // tests four byte utf-8 chars by themselves, and mixed with other types
        String[] inputs =
                new String[] {
                    "\uD852\uDF62",
                    "\uD800\uDD91",
                    "aaa\uD800\uDD91ä",
                    "\uD800\uDD94+zimmerm%C3%61dchen",
                    "\uD800\uDC00",
                    "\uD834\uDD61"
                };
        // Includes code points  U+10000, U+24B62, U+10191 that need a surrogate pair to represent
        // them

        for (String s : inputs) {
            testEncodeAndDecode(s, false, null);
            final StringBuilder inplaceBuilder = new StringBuilder(URLEncoder.encode(s, "UTF-8"));
            testEncodeAndDecode(s, true, inplaceBuilder);
        }

        // create a long string concatenating the above test input strings
        final StringBuilder longString = new StringBuilder(100);
        for (int k = 0; k < 50; k++) {
            for (String inp : inputs) {
                longString.append(inp);
            }
        }
        testEncodeAndDecode(longString.toString(), false, null);
    }

    private void testEncodeAndDecode(
            final String s, final boolean inPlace, final StringBuilder inPlaceBuilder)
            throws UnsupportedEncodingException {
        final String encode = URLEncoder.encode(s, "UTF-8");
        final String decoded = URLDecoder.decode(encode, "UTF-8");

        final StringBuilder sb = inPlace ? inPlaceBuilder : new StringBuilder();
        Preconditions.checkNotNull(sb);
        if (inPlace) {
            ParseUtils.urlDecodeInplace(sb);
        } else {
            ParseUtils.urlDecodeInto(encode, 0, encode.length(), sb);
        }
        final String actualDecoded = sb.toString();
        // asserts that results are same as original, and same as what URLDecoder.decode returns
        assertEquals(actualDecoded, decoded);
        assertEquals(actualDecoded, s);
    }

    private static void decodesTo(String from, String to) {
        StringBuilder sb = new StringBuilder(from.length());
        ParseUtils.urlDecodeInto(from, 0, from.length(), sb);
        assertEquals(to, sb.toString());
        StringBuilder sb2 = new StringBuilder(from);
        ParseUtils.urlDecodeInplace(sb2);
        assertEquals(to, sb2.toString());
    }

    private static void testIntParser(Random random, boolean testSigned) {
        final int k;
        if (testSigned) {
            k = random.nextInt();
        } else {
            k = random.nextInt(Integer.MAX_VALUE);
        }
        final String s = Integer.toString(k);
        final int actual;
        if (testSigned) {
            actual = ParseUtils.parseSignedInt(s, 0, s.length());
        } else {
            actual = ParseUtils.parseUnsignedInt(s, 0, s.length());
        }
        final int expected = Integer.parseInt(s);
        assertEquals(expected, actual);
    }

    private static void testLongParser(Random random) {
        final long l = random.nextLong();
        final String s = Long.toString(l);
        final long actual = ParseUtils.parseSignedLong(s, 0, s.length());
        final long expected = Long.parseLong(s);
        assertEquals(expected, actual);
    }

    private static void testFloatParser(Random random, boolean testSigned) {
        // test floats between 0 and 1
        final float smallFloat = testSigned ? -random.nextFloat() : random.nextFloat();
        testFloat(smallFloat);
        // test floats throughout the space
        final float f = Float.intBitsToFloat(random.nextInt());
        if (!Float.isNaN(
                f)) { // we don't support parsing the string "NaN" into its floating point form, and
            // intBitsToFloat returns NaN for certain bit ranges
            testFloat(f);
        }
    }

    private static void testFloat(float smallFloat) {
        final String s = Float.toString(smallFloat);
        final float actual = ParseUtils.parseFloat(s, 0, s.length());
        final float expected = Float.parseFloat(s);
        assertEquals(expected, actual);
    }
}
