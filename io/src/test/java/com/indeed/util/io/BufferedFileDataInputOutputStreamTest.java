package com.indeed.util.io;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

/**
 * @author kenh
 */

public class BufferedFileDataInputOutputStreamTest {
    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();


    @Test
    public void testFileInputOutput() throws IOException {
        final File somefile = tempDir.newFile("somefile");

        try (final BufferedFileDataOutputStream outputStream = new BufferedFileDataOutputStream(somefile)) {
            outputStream.writeInt(1);
            outputStream.writeInt(2);
            outputStream.writeInt(3);
            outputStream.writeInt(Integer.MAX_VALUE);
            outputStream.writeChar('0');
            outputStream.writeChars("abc");
            outputStream.writeFloat(1.2f);
            outputStream.writeDouble(Double.MAX_VALUE);
            outputStream.writeBoolean(true);
            outputStream.writeBoolean(false);
            outputStream.writeShort(5);
            outputStream.writeLong(Long.MAX_VALUE);
        }


        try (final BufferedFileDataInputStream inputStream = new BufferedFileDataInputStream(somefile)) {
            Assert.assertEquals(1, inputStream.readInt());
            Assert.assertEquals(2, inputStream.readInt());
            Assert.assertEquals(3, inputStream.readInt());
            Assert.assertEquals(Integer.MAX_VALUE, inputStream.readInt());
            Assert.assertEquals('0', inputStream.readChar());
            Assert.assertEquals('a', inputStream.readChar());
            Assert.assertEquals('b', inputStream.readChar());
            Assert.assertEquals('c', inputStream.readChar());
            Assert.assertEquals(1.2f, inputStream.readFloat(), 1e-9);
            Assert.assertEquals(Double.MAX_VALUE, inputStream.readDouble(), 1e-9);
            Assert.assertEquals(true, inputStream.readBoolean());
            Assert.assertEquals(false, inputStream.readBoolean());
            Assert.assertEquals(5, inputStream.readShort());
            Assert.assertEquals(Long.MAX_VALUE, inputStream.readLong());
        }
    }

    @Test
    public void testPathInputOutput() throws IOException {
        final File somefile = tempDir.newFile("somefile");

        try (final BufferedFileDataOutputStream outputStream = new BufferedFileDataOutputStream(somefile.toPath())) {
            outputStream.writeInt(1);
            outputStream.writeInt(2);
            outputStream.writeInt(3);
            outputStream.writeInt(Integer.MAX_VALUE);
            outputStream.writeChar('0');
            outputStream.writeChars("abc");
            outputStream.writeFloat(1.2f);
            outputStream.writeDouble(Double.MAX_VALUE);
            outputStream.writeBoolean(true);
            outputStream.writeBoolean(false);
            outputStream.writeShort(5);
            outputStream.writeLong(Long.MAX_VALUE);
        }


        try (final BufferedFileDataInputStream inputStream = new BufferedFileDataInputStream(somefile.toPath())) {
            Assert.assertEquals(1, inputStream.readInt());
            Assert.assertEquals(2, inputStream.readInt());
            Assert.assertEquals(3, inputStream.readInt());
            Assert.assertEquals(Integer.MAX_VALUE, inputStream.readInt());
            Assert.assertEquals('0', inputStream.readChar());
            Assert.assertEquals('a', inputStream.readChar());
            Assert.assertEquals('b', inputStream.readChar());
            Assert.assertEquals('c', inputStream.readChar());
            Assert.assertEquals(1.2f, inputStream.readFloat(), 1e-9);
            Assert.assertEquals(Double.MAX_VALUE, inputStream.readDouble(), 1e-9);
            Assert.assertEquals(true, inputStream.readBoolean());
            Assert.assertEquals(false, inputStream.readBoolean());
            Assert.assertEquals(5, inputStream.readShort());
            Assert.assertEquals(Long.MAX_VALUE, inputStream.readLong());
        }
    }
}