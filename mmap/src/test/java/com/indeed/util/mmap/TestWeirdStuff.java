package com.indeed.util.mmap;

import com.google.common.io.Files;
import com.google.common.io.LittleEndianDataOutputStream;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.Comparator;

/** @author jplaisance */
public final class TestWeirdStuff extends TestCase {

    private static final Logger log = LoggerFactory.getLogger(TestWeirdStuff.class);

    File tmpDir;

    File mmapFile;

    @Override
    public void setUp() throws Exception {
        tmpDir = java.nio.file.Files.createTempDirectory("weirdStuff").toFile();
    }

    private void writeFile() throws IOException {
        mmapFile = new File(tmpDir, "mmap");
        try (final LittleEndianDataOutputStream out =
                new LittleEndianDataOutputStream(
                        new BufferedOutputStream(new FileOutputStream(mmapFile), 65536))) {
            // write 8 mb of crap to ensure multiple pages have been written
            for (int i = 0; i < 2 * 1024 * 1024; i++) {
                out.writeInt(i);
            }
        }
    }

    public void tearDown() throws Exception {
        java.nio.file.Files.walk(tmpDir.toPath())
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    public void testTruncatedFileIndeedMMap() throws IOException {
        writeFile();
        try (final MMapBuffer buffer =
                new MMapBuffer(mmapFile, FileChannel.MapMode.READ_ONLY, ByteOrder.nativeOrder())) {
            final Memory memory = buffer.memory();
            // read half the file
            for (int i = 0; i < 1024 * 1024; i++) {
                assertEquals(i, memory.getInt(i * 4));
            }
            // trash the file
            try (final RandomAccessFile raf = new RandomAccessFile(mmapFile, "rw");
                    final FileChannel channel = raf.getChannel()) {
                channel.truncate(0);
                channel.force(true);
                // read the file again
                int i = 0;
                try {
                    for (i = 0; i < 2 * 1024 * 1024; i++) {
                        assertEquals(i, memory.getInt(i * 4));
                        assertTrue(false);
                    }
                } catch (final Throwable t) {
                    log.info("caught error reading address: " + i * 4, t);
                }
                // rewrite the file and see what happens
                writeFile();
                for (i = 0; i < 1024 * 1024; i++) {
                    assertEquals(i, memory.getInt(i * 4));
                }
            }
        }
    }

    public void testTruncatedFileJavaMMap() throws IOException {
        writeFile();
        final ByteBuffer buffer = Files.map(mmapFile, FileChannel.MapMode.READ_ONLY);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        // read half the file
        for (int i = 0; i < 1024 * 1024; i++) {
            assertEquals(i, buffer.getInt(i * 4));
        }
        // trash the file
        try (final RandomAccessFile raf = new RandomAccessFile(mmapFile, "rw");
                final FileChannel channel = raf.getChannel()) {
            channel.truncate(0);
            channel.force(true);
            // read the file again
            int i = 0;
            try {
                for (i = 0; i < 2 * 1024 * 1024; i++) {
                    assertEquals(i, buffer.getInt(i * 4));
                    assertTrue(false);
                }
            } catch (final Throwable t) {
                log.info("caught error reading address: " + i * 4, t);
            }
            // rewrite the file and see what happens
            writeFile();
            for (i = 0; i < 1024 * 1024; i++) {
                assertEquals(i, buffer.getInt(i * 4));
            }
        }
    }
}
