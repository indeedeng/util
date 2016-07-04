package com.indeed.util.mmap;

import com.google.common.collect.Iterables;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author jsgroth
 */
@SuppressWarnings("deprecation")
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

    @Test
    public void testTrackingDisabled() {
        MMapBuffer.setTrackingEnabled(false);
        assertFalse(MMapBuffer.isTrackingEnabled());
        assertNull(MMapBuffer.getTracker());
    }

    @Test
    public void testBuffersAreTracked() throws IOException {
        try {
            MMapBuffer.setTrackingEnabled(true);
            assertTrue(MMapBuffer.isTrackingEnabled());

            final MMapBuffer.Tracker tracker = MMapBuffer.getTracker();
            assertNotNull(tracker);

            final File tempFile = File.createTempFile("TestMMapBuffer", "");
            final MMapBuffer buffer = new MMapBuffer(tempFile, 0, 10, FileChannel.MapMode.READ_WRITE, ByteOrder.nativeOrder());

            try {
                assertTrue("A new buffer should be tracked!", Iterables.contains(tracker.getTrackedBuffers(), buffer));
            } finally {
                buffer.close();
            }

            assertFalse("Closed buffers should not be tracked!", Iterables.contains(tracker.getTrackedBuffers(), buffer));
        } finally {
            MMapBuffer.setTrackingEnabled(false);
        }
    }

    @Test
    public void testMadviseDontNeedTrackedBuffers() throws IOException {
        try {
            MMapBuffer.setTrackingEnabled(false);
            MMapBuffer.madviseDontNeedTrackedBuffers();

            MMapBuffer.setTrackingEnabled(true);
            MMapBuffer.madviseDontNeedTrackedBuffers();

            final File tempFile = File.createTempFile("TestMMapBuffer", "");
            try (MMapBuffer ignored = new MMapBuffer(tempFile, 0, 10, FileChannel.MapMode.READ_WRITE, ByteOrder.nativeOrder())) {
                MMapBuffer.madviseDontNeedTrackedBuffers();
            }

            MMapBuffer.madviseDontNeedTrackedBuffers();
        } finally {
            MMapBuffer.setTrackingEnabled(false);
        }
    }
}
