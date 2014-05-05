package com.indeed.util.io;


import com.google.common.io.LittleEndianDataOutputStream;
import org.apache.log4j.Logger;

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

/**
 * @author jplaisance
 */
public final class BufferedFileDataOutputStream extends OutputStream implements SyncableDataOutput {

    private static final Logger log = Logger.getLogger(BufferedFileDataOutputStream.class);

    private final ByteBuffer buffer;

    private final FileChannel channel;

    private final DataOutput dataOut;

    private final RandomAccessFile raf;

    public BufferedFileDataOutputStream(File file) throws FileNotFoundException {
        this(file, ByteOrder.BIG_ENDIAN);
    }

    public BufferedFileDataOutputStream(File file, ByteOrder order) throws FileNotFoundException {
        this(file, order, 131072);
    }

    public BufferedFileDataOutputStream(File file, ByteOrder order, int bufferSize) throws FileNotFoundException {
        raf = new RandomAccessFile(file, "rw");
        this.channel = raf.getChannel();
        buffer = ByteBuffer.allocate(bufferSize);
        if (order == ByteOrder.LITTLE_ENDIAN) {
            dataOut = new LittleEndianDataOutputStream(this);
        } else if (order == ByteOrder.BIG_ENDIAN) {
            dataOut = new DataOutputStream(this);
        } else {
            throw new IllegalArgumentException(order+" is not ByteOrder.BIG_ENDIAN or ByteOrder.LITTLE_ENDIAN");
        }
    }

    @Override
    public void write(final int b) throws IOException {
        if (buffer.remaining() == 0) {
            flush();
        }
        buffer.put((byte)b);
    }

    @Override
    public void write(final byte[] b, final int off, final int len) throws IOException {
        int current = off;
        final int end = off+len;
        while (current < end) {
            int size = Math.min(buffer.remaining(), end-current);
            if (size == 0) {
                flush();
                continue;
            }
            buffer.put(b, current, size);
            current+=size;
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
        channel.close();
        raf.close();
    }

    public long position() throws IOException {
        return channel.position()+buffer.position();
    }

    @Override
    public void sync() throws IOException {
        flush();
        channel.force(true);
    }
}
