package com.indeed.util.mmap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

/** @author jplaisance */
public final class DynamicMMapBufferDataOutputStream extends OutputStream implements DataOutput {

    private static final Logger log =
            LoggerFactory.getLogger(DynamicMMapBufferDataOutputStream.class);

    private MMapBuffer buffer;

    private Memory memory;

    private final File file;

    private final ByteOrder byteOrder;

    private long currentAddress = 0;

    private List<MMapBuffer> toClose = new ArrayList<MMapBuffer>();

    public DynamicMMapBufferDataOutputStream(final File file, final ByteOrder byteOrder)
            throws IOException {
        this.file = file;
        this.byteOrder = byteOrder;
        buffer = new MMapBuffer(file, 0, 4096, FileChannel.MapMode.READ_WRITE, byteOrder);
        memory = buffer.memory();
    }

    public DynamicMMapBufferDataOutputStream(
            final File file, final ByteOrder byteOrder, long offset) throws IOException {
        this.file = file;
        this.byteOrder = byteOrder;
        buffer =
                new MMapBuffer(
                        file,
                        0,
                        Math.max(4096, file.length()),
                        FileChannel.MapMode.READ_WRITE,
                        byteOrder);
        memory = buffer.memory();
        currentAddress = offset;
    }

    public DirectMemory memory() {
        return buffer.memory().slice(0, currentAddress);
    }

    public void writeBoolean(final boolean v) throws IOException {
        try {
            memory.putByte(currentAddress, (byte) (v ? 1 : 0));
            currentAddress++;
        } catch (IndexOutOfBoundsException e) {
            toClose.add(buffer);
            buffer =
                    new MMapBuffer(
                            file,
                            0,
                            memory.length() * 2,
                            FileChannel.MapMode.READ_WRITE,
                            byteOrder);
            memory = buffer.memory();
            writeBoolean(v);
        }
    }

    public void writeByte(final int v) throws IOException {
        try {
            memory.putByte(currentAddress, (byte) v);
            currentAddress++;
        } catch (IndexOutOfBoundsException e) {
            toClose.add(buffer);
            buffer =
                    new MMapBuffer(
                            file,
                            0,
                            memory.length() * 2,
                            FileChannel.MapMode.READ_WRITE,
                            byteOrder);
            memory = buffer.memory();
            writeByte(v);
        }
    }

    public void writeShort(final int v) throws IOException {
        try {
            memory.putShort(currentAddress, (short) v);
            currentAddress += 2;
        } catch (IndexOutOfBoundsException e) {
            toClose.add(buffer);
            buffer =
                    new MMapBuffer(
                            file,
                            0,
                            memory.length() * 2,
                            FileChannel.MapMode.READ_WRITE,
                            byteOrder);
            memory = buffer.memory();
            writeShort(v);
        }
    }

    public void writeChar(final int v) throws IOException {
        try {
            memory.putChar(currentAddress, (char) v);
            currentAddress += 2;
        } catch (IndexOutOfBoundsException e) {
            toClose.add(buffer);
            buffer =
                    new MMapBuffer(
                            file,
                            0,
                            memory.length() * 2,
                            FileChannel.MapMode.READ_WRITE,
                            byteOrder);
            memory = buffer.memory();
            writeChar(v);
        }
    }

    public void writeInt(final int v) throws IOException {
        try {
            memory.putInt(currentAddress, v);
            currentAddress += 4;
        } catch (IndexOutOfBoundsException e) {
            toClose.add(buffer);
            buffer =
                    new MMapBuffer(
                            file,
                            0,
                            memory.length() * 2,
                            FileChannel.MapMode.READ_WRITE,
                            byteOrder);
            memory = buffer.memory();
            writeInt(v);
        }
    }

    public void writeLong(final long v) throws IOException {
        try {
            memory.putLong(currentAddress, v);
            currentAddress += 8;
        } catch (IndexOutOfBoundsException e) {
            toClose.add(buffer);
            buffer =
                    new MMapBuffer(
                            file,
                            0,
                            memory.length() * 2,
                            FileChannel.MapMode.READ_WRITE,
                            byteOrder);
            memory = buffer.memory();
            writeLong(v);
        }
    }

    public void writeFloat(final float v) throws IOException {
        try {
            memory.putFloat(currentAddress, v);
            currentAddress += 4;
        } catch (IndexOutOfBoundsException e) {
            toClose.add(buffer);
            buffer =
                    new MMapBuffer(
                            file,
                            0,
                            memory.length() * 2,
                            FileChannel.MapMode.READ_WRITE,
                            byteOrder);
            memory = buffer.memory();
            writeFloat(v);
        }
    }

    public void writeDouble(final double v) throws IOException {
        try {
            memory.putDouble(currentAddress, v);
            currentAddress += 8;
        } catch (IndexOutOfBoundsException e) {
            toClose.add(buffer);
            buffer =
                    new MMapBuffer(
                            file,
                            0,
                            memory.length() * 2,
                            FileChannel.MapMode.READ_WRITE,
                            byteOrder);
            memory = buffer.memory();
            writeDouble(v);
        }
    }

    public void writeBytes(final String s) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void writeChars(final String s) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void writeUTF(final String s) throws IOException {
        throw new UnsupportedOperationException();
    }

    public void write(final int b) throws IOException {
        writeByte(b);
    }

    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        if (off < 0 || len < 0 || off + len > b.length) {
            throw new IndexOutOfBoundsException();
        }
        try {
            memory.putBytes(currentAddress, b, off, len);
            currentAddress += len;
        } catch (IndexOutOfBoundsException e) {
            toClose.add(buffer);
            buffer =
                    new MMapBuffer(
                            file,
                            0,
                            memory.length() * 2,
                            FileChannel.MapMode.READ_WRITE,
                            byteOrder);
            memory = buffer.memory();
            write(b, off, len);
        }
    }

    @Override
    public void close() throws IOException {
        for (MMapBuffer buffer : toClose) {
            buffer.close();
        }
        buffer.close();
    }

    public long position() {
        return currentAddress;
    }

    public void sync() throws IOException {
        buffer.sync(0, currentAddress);
    }
}
