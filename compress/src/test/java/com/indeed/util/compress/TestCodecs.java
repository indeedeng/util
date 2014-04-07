package com.indeed.util.compress;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import junit.framework.TestCase;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * @author jplaisance
 */
public final class TestCodecs extends TestCase {
    private static final Logger log = Logger.getLogger(TestCodecs.class);

    public static void testGzip() throws IOException {
        GzipCodec codec = new GzipCodec();
        testCodec(codec);
        testEmpty(codec);
    }

    public static void testSnappy() throws IOException {
        SnappyCodec codec = new SnappyCodec();
        testCodec(codec);
        testEmpty(codec);
    }

    public static void testCodec(CompressionCodec codec) throws IOException {
        for (int i = 0; i < 10; i++) {
            final File file = new File("src/test/resources/jobsearchlogs.out");
            final byte[] original = Files.toByteArray(file);
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            final CompressionOutputStream compressionOutputStream = codec.createOutputStream(out);
            final FileInputStream fileIn = new FileInputStream(file);
            long start = System.nanoTime();
            ByteStreams.copy(fileIn, compressionOutputStream);
            compressionOutputStream.close();
            double ms = (System.nanoTime() - start) / 1000000d;
            System.out.println("compression time: "+ ms +" ms, compression speed: "+(long)(original.length/(ms/1000))+" bytes per second");
            final ByteArrayOutputStream copy = new ByteArrayOutputStream();
            final byte[] compressed = out.toByteArray();
            final InputStream in = codec.createInputStream(new ByteArrayInputStream(compressed));
            start = System.nanoTime();
            ByteStreams.copy(in, copy);
            ms = (System.nanoTime() - start) / 1000000d;
            System.out.println("decompression time: "+ ms +" ms, decompression speed: "+(long)(original.length/(ms/1000))+" bytes per second");
            in.close();
            final byte[] copyBytes = copy.toByteArray();
            assertEquals(original.length, copyBytes.length);
            assertTrue(Arrays.equals(original, copyBytes));
        }
    }

    public static void testEmpty(CompressionCodec codec) throws IOException {
        final byte[] original = new byte[0];
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final CompressionOutputStream compressionOutputStream = codec.createOutputStream(out);
        compressionOutputStream.write(original);
        compressionOutputStream.close();
        final ByteArrayOutputStream copy = new ByteArrayOutputStream();
        final byte[] compressed = out.toByteArray();
        final InputStream in = codec.createInputStream(new ByteArrayInputStream(compressed));
        ByteStreams.copy(in, copy);
        final byte[] copyBytes = copy.toByteArray();
        assertEquals(original.length, copyBytes.length);
        assertTrue(Arrays.equals(original, copyBytes));
    }
}
