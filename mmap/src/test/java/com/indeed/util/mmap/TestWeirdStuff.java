package com.indeed.util.mmap;

import com.google.common.io.Files;
import com.google.common.io.LittleEndianDataOutputStream;
import com.indeed.util.core.shell.PosixFileOperations;
import junit.framework.TestCase;
import org.apache.log4j.Logger;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

/**
 * @author jplaisance
 */
public final class TestWeirdStuff extends TestCase {

    private static final Logger log = Logger.getLogger(TestWeirdStuff.class);

    File tmpDir;

    File mmapFile;

    @Override
    public void setUp() throws Exception {
        tmpDir = Files.createTempDir();
    }

    private void writeFile() throws IOException {
        mmapFile = new File(tmpDir, "mmap");
        LittleEndianDataOutputStream out = new LittleEndianDataOutputStream(new BufferedOutputStream(new FileOutputStream(mmapFile), 65536));
        //write 8 mb of crap to ensure multiple pages have been written
        for (int i = 0; i < 2 * 1024 * 1024; i++) {
            out.writeInt(i);
        }
        out.close();
    }

    @Override
    public void tearDown() throws Exception {
        PosixFileOperations.rmrf(tmpDir);
    }

    public void testTruncatedFileIndeedMMap() throws IOException {
        writeFile();
        MMapBuffer buffer = new MMapBuffer(mmapFile, FileChannel.MapMode.READ_ONLY, ByteOrder.nativeOrder());
        Memory memory = buffer.memory();
        //read half the file
        for (int i = 0; i < 1024 * 1024; i++) {
            assertEquals(i, memory.getInt(i * 4));
        }
        //trash the file
        RandomAccessFile raf = new RandomAccessFile(mmapFile, "rw");
        FileChannel channel = raf.getChannel();
        channel.truncate(0);
        channel.force(true);
        //read the file again
        int i = 0;
        try {
            for (i = 0; i < 2 * 1024 * 1024; i++) {
                assertEquals(i, memory.getInt(i * 4));
                assertTrue(false);
            }
        } catch (Throwable t) {
            log.info("caught error reading address: " + i * 4, t);
        }
        //rewrite the file and see what happens
        writeFile();
        for (i = 0; i < 1024 * 1024; i++) {
            assertEquals(i, memory.getInt(i * 4));
        }
        channel.close();
        raf.close();
        buffer.close();
    }

    public void testTruncatedFileJavaMMap() throws IOException {
        writeFile();
        ByteBuffer buffer = Files.map(mmapFile, FileChannel.MapMode.READ_ONLY);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        //read half the file
        for (int i = 0; i < 1024 * 1024; i++) {
            assertEquals(i, buffer.getInt(i * 4));
        }
        //trash the file
        RandomAccessFile raf = new RandomAccessFile(mmapFile, "rw");
        FileChannel channel = raf.getChannel();
        channel.truncate(0);
        channel.force(true);
        //read the file again
        int i = 0;
        try {
            for (i = 0; i < 2 * 1024 * 1024; i++) {
                assertEquals(i, buffer.getInt(i * 4));
                assertTrue(false);
            }
        } catch (Throwable t) {
            log.info("caught error reading address: " + i * 4, t);
        }
        //rewrite the file and see what happens
        writeFile();
        for (i = 0; i < 1024 * 1024; i++) {
            assertEquals(i, buffer.getInt(i * 4));
        }
        channel.close();
        raf.close();
    }
}
