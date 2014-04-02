package com.indeed.util.mmap;

import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import static org.junit.Assert.assertArrayEquals;

/**
 * @author jsgroth
 */
public class TestMMapBuffer {
    @Test(expected = FileNotFoundException.class)
    public void testReadNoFile() throws IOException {
        File f = File.createTempFile("asdf", "");
        f.delete();
        MMapBuffer buffer = new MMapBuffer(f, FileChannel.MapMode.READ_ONLY, ByteOrder.nativeOrder());
        buffer.close();
    }

    @Test
    public void testWriteNoFile() throws IOException {
        File f = File.createTempFile("asdf", "");
        f.delete();

        MMapBuffer buffer = new MMapBuffer(f, 0, 10, FileChannel.MapMode.READ_WRITE, ByteOrder.nativeOrder());
        DirectMemory memory = buffer.memory();
        memory.putBytes(0, new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
        buffer.sync(0, memory.length());
        buffer.close();

        buffer = new MMapBuffer(f, FileChannel.MapMode.READ_ONLY, ByteOrder.nativeOrder());
        memory = buffer.memory();
        byte[] bytes = new byte[10];
        memory.getBytes(0, bytes);
        assertArrayEquals(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, bytes);
        buffer.close();
    }
}
