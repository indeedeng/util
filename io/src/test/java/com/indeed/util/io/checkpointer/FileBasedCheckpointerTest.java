package com.indeed.util.io.checkpointer;

import com.indeed.util.serialization.LongStringifier;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

/** @author kenh */
public class FileBasedCheckpointerTest {
    @Rule public TemporaryFolder tempDir = new TemporaryFolder();

    @Test
    public void testLongUpdates() throws IOException {
        final File somefile = tempDir.newFile("somefile");
        final Checkpointer<Long> checkpointer =
                new FileBasedCheckpointer<>(somefile, new LongStringifier(), 0L);
        Assert.assertEquals(Long.valueOf(0), checkpointer.getCheckpoint());
        checkpointer.setCheckpoint(1234L);
        Assert.assertEquals(Long.valueOf(1234), checkpointer.getCheckpoint());
        Assert.assertEquals(
                Long.valueOf(1234),
                new FileBasedCheckpointer<>(somefile, new LongStringifier(), 0L).getCheckpoint());
        Assert.assertEquals(
                Long.valueOf(1234),
                new FileBasedCheckpointer<>(somefile.toPath(), new LongStringifier(), 0L)
                        .getCheckpoint());

        checkpointer.setCheckpoint(4321L);
        Assert.assertEquals(Long.valueOf(4321), checkpointer.getCheckpoint());
        Assert.assertEquals(
                Long.valueOf(4321),
                new FileBasedCheckpointer<>(somefile, new LongStringifier(), 0L).getCheckpoint());
        Assert.assertEquals(
                Long.valueOf(4321),
                new FileBasedCheckpointer<>(somefile.toPath(), new LongStringifier(), 0L)
                        .getCheckpoint());
    }
}
