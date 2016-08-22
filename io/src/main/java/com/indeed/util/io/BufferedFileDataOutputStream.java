package com.indeed.util.io;

import com.google.common.io.Closer;
import com.google.common.io.LittleEndianDataOutputStream;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * @author jplaisance
 */
public final class BufferedFileDataOutputStream extends OutputStream implements SyncableDataOutput {
    private static final int DEFAULT_BUFFER_SIZE = 131072;

    private final ByteBuffer buffer;

    private final FileChannel channel;

    private final DataOutput dataOut;

    private final Closer closer = Closer.create();

    public BufferedFileDataOutputStream(final File file) throws FileNotFoundException {
        this(file, ByteOrder.BIG_ENDIAN);
    }

    public BufferedFileDataOutputStream(final File file, final ByteOrder order) throws FileNotFoundException {
        this(file, order, DEFAULT_BUFFER_SIZE);
    }

    public BufferedFileDataOutputStream(final File file, final ByteOrder order, final int bufferSize) throws FileNotFoundException {
        // for backwards compatiblity with file interface, we still use RandomAccessFile
        final RandomAccessFile raf = new RandomAccessFile(file, "rw");
        closer.register(raf);
        channel = raf.getChannel();
        closer.register(channel);

        buffer = ByteBuffer.allocate(bufferSize);
        if (order == ByteOrder.LITTLE_ENDIAN) {
            dataOut = new LittleEndianDataOutputStream(this);
        } else if (order == ByteOrder.BIG_ENDIAN) {
            dataOut = new DataOutputStream(this);
        } else {
            throw new IllegalArgumentException(order + " is not ByteOrder.BIG_ENDIAN or ByteOrder.LITTLE_ENDIAN");
        }
    }

    public BufferedFileDataOutputStream(final Path path) throws IOException {
        this(path, ByteOrder.BIG_ENDIAN);
    }

    public BufferedFileDataOutputStream(final Path path, final ByteOrder order) throws IOException {
        this(path, order, DEFAULT_BUFFER_SIZE);
    }

    public BufferedFileDataOutputStream(final Path path, final ByteOrder order, final int bufferSize) throws IOException {
        channel = FileChannel.open(path,
                StandardOpenOption.CREATE,
                StandardOpenOption.READ,
                StandardOpenOption.WRITE);
        closer.register(channel);

        buffer = ByteBuffer.allocate(bufferSize);
        if (order == ByteOrder.LITTLE_ENDIAN) {
            dataOut = new LittleEndianDataOutputStream(this);
        } else if (order == ByteOrder.BIG_ENDIAN) {
            dataOut = new DataOutputStream(this);
        } else {
            throw new IllegalArgumentException(order + " is not ByteOrder.BIG_ENDIAN or ByteOrder.LITTLE_ENDIAN");
        }
    }

    @Override
    public void write(final int b) throws IOException {
        if (buffer.remaining() == 0) {
            flush();
        }
        buffer.put((byte) b);
    }

    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        int current = off;
        final int end = off + len;
        while (current < end) {
            int size = Math.min(buffer.remaining(), end - current);
            if (size == 0) {
                flush();
                continue;
            }
            buffer.put(b, current, size);
            current += size;
        }
    }

    @Override
    public void writeBoolean(final boolean v) throws IOException {
        dataOut.writeBoolean(v);
    }

    @Override
    public void writeByte(final int v) throws IOException {
        dataOut.writeByte(v);
    }

    @Override
    public void writeShort(final int v) throws IOException {
        dataOut.writeShort(v);
    }

    @Override
    public void writeChar(final int v) throws IOException {
        dataOut.writeChar(v);
    }

    @Override
    public void writeInt(final int v) throws IOException {
        dataOut.writeInt(v);
    }

    @Override
    public void writeLong(final long v) throws IOException {
        dataOut.writeLong(v);
    }

    @Override
    public void writeFloat(final float v) throws IOException {
        dataOut.writeFloat(v);
    }

    @Override
    public void writeDouble(final double v) throws IOException {
        dataOut.writeDouble(v);
    }

    @Override
    public void writeBytes(final String s) throws IOException {
        dataOut.writeBytes(s);
    }

    @Override
    public void writeChars(final String s) throws IOException {
        dataOut.writeChars(s);
    }

    @Override
    public void writeUTF(final String str) throws IOException {
        dataOut.writeUTF(str);
    }

    @Override
    public void flush() throws IOException {
        buffer.flip();
        channel.write(buffer);
        buffer.clear();
    }

    @Override
    public void close() throws IOException {
        flush();
        closer.close();
    }

    public long position() throws IOException {
        return channel.position() + buffer.position();
    }

    @Override
    public void sync() throws IOException {
        flush();
        channel.force(true);
    }
}
