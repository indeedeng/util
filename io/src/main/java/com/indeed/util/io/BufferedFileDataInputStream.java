package com.indeed.util.io;

import com.google.common.io.LittleEndianDataInputStream;
import org.apache.log4j.Logger;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

/**
 * @author jplaisance
 */
public final class BufferedFileDataInputStream extends InputStream implements DataInput, Seekable {

    private static final Logger log = Logger.getLogger(BufferedFileDataInputStream.class);

    private final RandomAccessFile raf;

    private final FileChannel channel;

    private final ByteBuffer buffer;

    private long bufferPos;

    private final DataInput dataInput;

    public BufferedFileDataInputStream(File file) throws FileNotFoundException {
        this(file, ByteOrder.BIG_ENDIAN);
    }

    public BufferedFileDataInputStream(File file, ByteOrder order) throws FileNotFoundException {
        this(file, order, 131072);
    }

    public BufferedFileDataInputStream(File file, ByteOrder order, int bufferSize) throws FileNotFoundException {
        raf = new RandomAccessFile(file, "r");
        channel = raf.getChannel();
        buffer = ByteBuffer.allocate(bufferSize);
        buffer.limit(0);
        if (order == ByteOrder.BIG_ENDIAN) {
            dataInput = new DataInputStream(this);
        } else {
            dataInput = new LittleEndianDataInputStream(this);
        }
    }

    private boolean fillBuffer() throws IOException {
        buffer.position(0);
        int limit = (int)Math.min((channel.size() - channel.position()), buffer.capacity());
        buffer.limit(limit);
        if (limit == 0) return false;
        bufferPos = channel.position();
        channel.read(buffer);
        buffer.flip();
        return true;
    }

    @Override
    public int read() throws IOException {
        if (buffer.remaining() == 0) {
            if (!fillBuffer()) return -1;
        }
        return buffer.get()&0xFF;
    }

    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        if (buffer.remaining() == 0) {
            if (!fillBuffer()) return -1;
        }
        int read = Math.min(len, buffer.remaining());
        buffer.get(b, off, read);
        return read;
    }

    public long position() throws IOException {
        return bufferPos+buffer.position();
    }

    public long length() throws IOException {
        return channel.size();
    }

    public void seek(long addr) throws IOException {
        if (addr >= bufferPos && addr <= bufferPos+buffer.limit()) {
            buffer.position((int)(addr-bufferPos));
        } else {
            channel.position(addr);
            bufferPos = addr;
            buffer.position(0);
            buffer.limit(0);
        }
    }

    @Override
    public void close() throws IOException {
        channel.close();
        raf.close();
    }

    @Override
    public String readUTF() throws IOException {
        return dataInput.readUTF();
    }

    @Override
    public String readLine() throws IOException {
        return dataInput.readLine();
    }

    @Override
    public double readDouble() throws IOException {
        return dataInput.readDouble();
    }

    @Override
    public float readFloat() throws IOException {
        return dataInput.readFloat();
    }

    @Override
    public long readLong() throws IOException {
        return dataInput.readLong();
    }

    @Override
    public int readInt() throws IOException {
        return dataInput.readInt();
    }

    @Override
    public char readChar() throws IOException {
        return dataInput.readChar();
    }

    @Override
    public int readUnsignedShort() throws IOException {
        return dataInput.readUnsignedShort();
    }

    @Override
    public short readShort() throws IOException {
        return dataInput.readShort();
    }

    @Override
    public boolean readBoolean() throws IOException {
        return dataInput.readBoolean();
    }

    @Override
    public int skipBytes(final int n) throws IOException {
        return dataInput.skipBytes(n);
    }

    @Override
    public void readFully(final byte[] b, final int off, final int len) throws IOException {
        dataInput.readFully(b, off, len);
    }

    @Override
    public void readFully(final byte[] b) throws IOException {
        dataInput.readFully(b);
    }

    @Override
    public byte readByte() throws IOException {
        return dataInput.readByte();
    }

    @Override
    public int readUnsignedByte() throws IOException {
        return dataInput.readUnsignedByte();
    }
}
