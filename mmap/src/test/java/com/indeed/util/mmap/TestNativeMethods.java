package com.indeed.util.mmap;

import com.google.common.primitives.Bytes;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.Random;

/**
 * @author jsgroth
 */
public class TestNativeMethods extends TestCase {
    private File file;

    @Override
    protected void setUp() throws Exception {
        file = File.createTempFile("squall-mmap-test", ".bin");
    }

    @Override
    protected void tearDown() throws Exception {
        file.delete();
    }

    @Test
    public void testMMapMUnmapErrno() throws IOException {
        write10BytesToFile();

        MMapBuffer buffer = new MMapBuffer(file, FileChannel.MapMode.READ_ONLY, ByteOrder.nativeOrder());
        try {
            buffer.getErrno();
        } finally {
            buffer.close();
        }
    }

    private void write10BytesToFile() throws IOException {
        OutputStream os = new FileOutputStream(file);
        Random rand = new Random();
        for (int i = 0; i < 10; ++i) {
            os.write(rand.nextInt(256));
        }
        os.close();
    }

    @Test
    public void testCopyFromAndToByteArray() throws IOException {
        MMapBuffer buffer = new MMapBuffer(file, 0L, 10L, FileChannel.MapMode.READ_WRITE, ByteOrder.nativeOrder());
        Memory memory = buffer.memory();
        byte[] bytes = new byte[10];
        try {
            Random rand = new Random();
            rand.nextBytes(bytes);
            memory.putBytes(0L, bytes);
        } finally {
            buffer.close();
        }

        MMapBuffer buffer2 = new MMapBuffer(file, FileChannel.MapMode.READ_ONLY, ByteOrder.nativeOrder());
        Memory memory2 = buffer2.memory();
        try {
            byte[] bytes2 = new byte[bytes.length];
            memory2.getBytes(0L, bytes2);
            assertEquals(Bytes.asList(bytes), Bytes.asList(bytes2));
        } finally {
            buffer2.close();
        }
    }

    @Test
    public void testSyncAdvise() throws IOException {
        MMapBuffer buffer = new MMapBuffer(file, 0L, 10L, FileChannel.MapMode.READ_WRITE, ByteOrder.nativeOrder());
        Memory memory = buffer.memory();
        try {
            memory.putBytes(0L, new byte[10]);
            buffer.sync(0L, 10L);
        } finally {
            buffer.close();
        }

        MMapBuffer buffer2 = new MMapBuffer(file, FileChannel.MapMode.READ_ONLY, ByteOrder.nativeOrder());
        Memory memory2 = buffer2.memory();
        try {
            buffer2.advise(0L, 10L);
            byte[] bytes = new byte[10];
            memory2.getBytes(0L, bytes);
        } finally {
            buffer.close();
        }
    }

    @Test
    public void testMLock() throws IOException {
        MMapBuffer buffer = new MMapBuffer(file, 0L, 10L, FileChannel.MapMode.READ_WRITE, ByteOrder.nativeOrder());
        buffer.memory().putByte(0, (byte)1);
        buffer.mlock(0, buffer.memory().length());
        buffer.memory().putByte(0, (byte)2);
        buffer.munlock(0, buffer.memory().length());
        buffer.memory().putByte(0, (byte)3);
        buffer.close();
    }
}
