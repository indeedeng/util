package com.indeed.util.serialization;

import org.apache.log4j.Logger;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author jplaisance
 */
public final class VIntUtils {

    private static final Logger log = Logger.getLogger(VIntUtils.class);

    public static int writeVInt(OutputStream out, int i) throws IOException {
        // blatantly copied from lucene's IndexOutput.writeVInt();
        int bytes = 1;
        while ((i & ~0x7F) != 0) {
            out.write((byte)((i & 0x7f) | 0x80));
            i >>>= 7;
            bytes++;
        }
        out.write((byte)i);
        return bytes;
    }

    public static int readVInt(InputStream in) throws IOException {
        // blatantly copied from lucene's IndexInput.readVInt();
        byte b = (byte)in.read();
        int i = b & 0x7F;
        for (int shift = 7; (b & 0x80) != 0; shift += 7) {
            b = (byte)in.read();
            i |= (b & 0x7F) << shift;
        }
        return i;
    }

    public static int writeSVInt(OutputStream out, int i) throws IOException {
        return writeVInt(out, (i << 1) ^ (i >> 31));
    }

    public static int readSVInt(InputStream in) throws IOException {
        int i = readVInt(in);
        return (i >>> 1) ^ (-(i & 1));
    }

    public static int writeVInt(DataOutput out, int i) throws IOException {
        // blatantly copied from lucene's IndexOutput.writeVInt();
        int bytes = 1;
        while ((i & ~0x7F) != 0) {
            out.write((byte)((i & 0x7f) | 0x80));
            i >>>= 7;
            bytes++;
        }
        out.write((byte)i);
        return bytes;
    }

    public static int readVInt(DataInput in) throws IOException {
        // blatantly copied from lucene's IndexInput.readVInt();
        byte b = in.readByte();
        int i = b & 0x7F;
        for (int shift = 7; (b & 0x80) != 0; shift += 7) {
            b = in.readByte();
            i |= (b & 0x7F) << shift;
        }
        return i;
    }

    public static int writeSVInt(DataOutput out, int i) throws IOException {
        return writeVInt(out, (i << 1) ^ (i >> 31));
    }

    public static int readSVInt(DataInput in) throws IOException {
        int i = readVInt(in);
        return (i >>> 1) ^ (-(i & 1));
    }
    
    public static int getVIntLength(int i) {
        int ret = 0;
        do {
            ret++;
            i>>>=7;
        } while (i > 0);
        return ret;
    }

    public static int writeVInt64(OutputStream out, long i) throws IOException {
        // blatantly copied from lucene's IndexOutput.writeVInt();
        int bytes = 1;
        while ((i & ~0x7F) != 0) {
            out.write((byte)((i & 0x7f) | 0x80));
            i >>>= 7;
            bytes++;
        }
        out.write((byte)i);
        return bytes;
    }

    public static long readVInt64(InputStream in) throws IOException {
        // blatantly copied from lucene's IndexInput.readVInt();
        byte b = (byte)in.read();
        long i = b & 0x7F;
        for (long shift = 7; (b & 0x80) != 0; shift += 7) {
            b = (byte)in.read();
            i |= (b & 0x7FL) << shift;
        }
        return i;
    }

    public static int writeSVInt64(OutputStream out, long i) throws IOException {
        return writeVInt64(out, (i << 1) ^ (i >> 63));
    }

    public static long readSVInt64(InputStream in) throws IOException {
        long i = readVInt64(in);
        return (i >>> 1) ^ (-(i & 1));
    }

    public static int writeVInt64(DataOutput out, long i) throws IOException {
        // blatantly copied from lucene's IndexOutput.writeVInt();
        int bytes = 1;
        while ((i & ~0x7F) != 0) {
            out.write((byte)((i & 0x7f) | 0x80));
            i >>>= 7;
            bytes++;
        }
        out.write((byte)i);
        return bytes;
    }

    public static long readVInt64(DataInput in) throws IOException {
        // blatantly copied from lucene's IndexInput.readVInt();
        byte b = in.readByte();
        long i = b & 0x7F;
        for (long shift = 7; (b & 0x80) != 0; shift += 7) {
            b = in.readByte();
            i |= (b & 0x7FL) << shift;
        }
        return i;
    }

    public static int writeSVInt64(DataOutput out, long i) throws IOException {
        return writeVInt64(out, (i << 1) ^ (i >> 63));
    }

    public static long readSVInt64(DataInput in) throws IOException {
        long i = readVInt64(in);
        return (i >>> 1) ^ (-(i & 1));
    }
    
    public static int getVInt64Length(long l) {
        int ret = 0;
        do {
            ret++;
            l>>>=7;
        } while (l > 0);
        return ret;
    }
}
