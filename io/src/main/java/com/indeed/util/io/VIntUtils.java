package com.indeed.util.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author jplaisance
 */
public final class VIntUtils {

    private static final Logger log = LoggerFactory.getLogger(VIntUtils.class);

    public static int writeVInt(OutputStream out, int i) throws IOException {
        if (i < 0) {
            out.write((i&0x7F) | 0x80);
            out.write(((i>>>7)&0x7F) | 0x80);
            out.write(((i>>>14)&0x7F) | 0x80);
            out.write(((i>>>21)&0x7F) | 0x80);
            out.write(i>>>28);
            return 5;
        } else if (i < 1 << 7) {
            out.write(i);
            return 1;
        } else if (i < 1 << 14) {
            out.write((i&0x7F) | 0x80);
            out.write(i>>>7);
            return 2;
        } else if (i < 1 << 21) {
            out.write((i&0x7F) | 0x80);
            out.write(((i>>>7)&0x7F) | 0x80);
            out.write(i>>>14);
            return 3;
        } else if (i < 1 << 28) {
            out.write((i&0x7F) | 0x80);
            out.write(((i>>>7)&0x7F) | 0x80);
            out.write(((i>>>14)&0x7F) | 0x80);
            out.write(i>>>21);
            return 4;
        } else {
            out.write((i&0x7F) | 0x80);
            out.write(((i>>>7)&0x7F) | 0x80);
            out.write(((i>>>14)&0x7F) | 0x80);
            out.write(((i>>>21)&0x7F) | 0x80);
            out.write(i>>>28);
            return 5;
        }
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
        if (i < 0) {
            out.write((i&0x7F) | 0x80);
            out.write(((i>>>7)&0x7F) | 0x80);
            out.write(((i>>>14)&0x7F) | 0x80);
            out.write(((i>>>21)&0x7F) | 0x80);
            out.write(i>>>28);
            return 5;
        } else if (i < 1 << 7) {
            out.write(i);
            return 1;
        } else if (i < 1 << 14) {
            out.write((i&0x7F) | 0x80);
            out.write(i>>>7);
            return 2;
        } else if (i < 1 << 21) {
            out.write((i&0x7F) | 0x80);
            out.write(((i>>>7)&0x7F) | 0x80);
            out.write(i>>>14);
            return 3;
        } else if (i < 1 << 28) {
            out.write((i&0x7F) | 0x80);
            out.write(((i>>>7)&0x7F) | 0x80);
            out.write(((i>>>14)&0x7F) | 0x80);
            out.write(i>>>21);
            return 4;
        } else {
            out.write((i&0x7F) | 0x80);
            out.write(((i>>>7)&0x7F) | 0x80);
            out.write(((i>>>14)&0x7F) | 0x80);
            out.write(((i>>>21)&0x7F) | 0x80);
            out.write(i>>>28);
            return 5;
        }
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
        if (i < 0) {
            out.write((int) ((i&0x7F) | 0x80));
            out.write((int) (((i>>>7)&0x7F) | 0x80));
            out.write((int) (((i>>>14)&0x7F) | 0x80));
            out.write((int) (((i>>>21)&0x7F) | 0x80));
            out.write((int) (((i>>>28)&0x7F) | 0x80));
            out.write((int) (((i>>>35)&0x7F) | 0x80));
            out.write((int) (((i>>>42)&0x7F) | 0x80));
            out.write((int) (((i>>>49)&0x7F) | 0x80));
            out.write((int) (((i>>>56)&0x7F) | 0x80));
            out.write((int) (i>>>63));
            return 10;
        } else if (i < 1L << 7) {
            out.write((int)i);
            return 1;
        } else if (i < 1L << 14) {
            out.write((int) ((i&0x7F) | 0x80));
            out.write((int) (i>>>7));
            return 2;
        } else if (i < 1L << 21) {
            out.write((int) ((i&0x7F) | 0x80));
            out.write((int) (((i>>>7)&0x7F) | 0x80));
            out.write((int) (i>>>14));
            return 3;
        } else if (i < 1L << 28) {
            out.write((int) ((i&0x7F) | 0x80));
            out.write((int) (((i>>>7)&0x7F) | 0x80));
            out.write((int) (((i>>>14)&0x7F) | 0x80));
            out.write((int) (i>>>21));
            return 4;
        } else if (i < 1L << 35) {
            out.write((int) ((i&0x7F) | 0x80));
            out.write((int) (((i>>>7)&0x7F) | 0x80));
            out.write((int) (((i>>>14)&0x7F) | 0x80));
            out.write((int) (((i>>>21)&0x7F) | 0x80));
            out.write((int) (i>>>28));
            return 5;
        } else if (i < 1L << 42) {
            out.write((int) ((i&0x7F) | 0x80));
            out.write((int) (((i>>>7)&0x7F) | 0x80));
            out.write((int) (((i>>>14)&0x7F) | 0x80));
            out.write((int) (((i>>>21)&0x7F) | 0x80));
            out.write((int) (((i>>>28)&0x7F) | 0x80));
            out.write((int) (i>>>35));
            return 6;
        } else if (i < 1L << 49) {
            out.write((int) ((i&0x7F) | 0x80));
            out.write((int) (((i>>>7)&0x7F) | 0x80));
            out.write((int) (((i>>>14)&0x7F) | 0x80));
            out.write((int) (((i>>>21)&0x7F) | 0x80));
            out.write((int) (((i>>>28)&0x7F) | 0x80));
            out.write((int) (((i>>>35)&0x7F) | 0x80));
            out.write((int) (i>>>42));
            return 7;
        } else if (i < 1L << 56) {
            out.write((int) ((i&0x7F) | 0x80));
            out.write((int) (((i>>>7)&0x7F) | 0x80));
            out.write((int) (((i>>>14)&0x7F) | 0x80));
            out.write((int) (((i>>>21)&0x7F) | 0x80));
            out.write((int) (((i>>>28)&0x7F) | 0x80));
            out.write((int) (((i>>>35)&0x7F) | 0x80));
            out.write((int) (((i>>>42)&0x7F) | 0x80));
            out.write((int) (i>>>49));
            return 8;
        } else {
            out.write((int) ((i&0x7F) | 0x80));
            out.write((int) (((i>>>7)&0x7F) | 0x80));
            out.write((int) (((i>>>14)&0x7F) | 0x80));
            out.write((int) (((i>>>21)&0x7F) | 0x80));
            out.write((int) (((i>>>28)&0x7F) | 0x80));
            out.write((int) (((i>>>35)&0x7F) | 0x80));
            out.write((int) (((i>>>42)&0x7F) | 0x80));
            out.write((int) (((i>>>49)&0x7F) | 0x80));
            out.write((int) (i>>>56));
            return 9;
        }
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
        if (i < 0) {
            out.write((int) ((i&0x7F) | 0x80));
            out.write((int) (((i>>>7)&0x7F) | 0x80));
            out.write((int) (((i>>>14)&0x7F) | 0x80));
            out.write((int) (((i>>>21)&0x7F) | 0x80));
            out.write((int) (((i>>>28)&0x7F) | 0x80));
            out.write((int) (((i>>>35)&0x7F) | 0x80));
            out.write((int) (((i>>>42)&0x7F) | 0x80));
            out.write((int) (((i>>>49)&0x7F) | 0x80));
            out.write((int) (((i>>>56)&0x7F) | 0x80));
            out.write((int)(i>>>63));
            return 10;
        } else if (i < 1L << 7) {
            out.write((int)i);
            return 1;
        } else if (i < 1L << 14) {
            out.write((int) ((i&0x7F) | 0x80));
            out.write((int)(i>>>7));
            return 2;
        } else if (i < 1L << 21) {
            out.write((int) ((i&0x7F) | 0x80));
            out.write((int) (((i>>>7)&0x7F) | 0x80));
            out.write((int)(i>>>14));
            return 3;
        } else if (i < 1L << 28) {
            out.write((int) ((i&0x7F) | 0x80));
            out.write((int) (((i>>>7)&0x7F) | 0x80));
            out.write((int) (((i>>>14)&0x7F) | 0x80));
            out.write((int)(i>>>21));
            return 4;
        } else if (i < 1L << 35) {
            out.write((int) ((i&0x7F) | 0x80));
            out.write((int) (((i>>>7)&0x7F) | 0x80));
            out.write((int) (((i>>>14)&0x7F) | 0x80));
            out.write((int) (((i>>>21)&0x7F) | 0x80));
            out.write((int)(i>>>28));
            return 5;
        } else if (i < 1L << 42) {
            out.write((int) ((i&0x7F) | 0x80));
            out.write((int) (((i>>>7)&0x7F) | 0x80));
            out.write((int) (((i>>>14)&0x7F) | 0x80));
            out.write((int) (((i>>>21)&0x7F) | 0x80));
            out.write((int) (((i>>>28)&0x7F) | 0x80));
            out.write((int)(i>>>35));
            return 6;
        } else if (i < 1L << 49) {
            out.write((int) ((i&0x7F) | 0x80));
            out.write((int) (((i>>>7)&0x7F) | 0x80));
            out.write((int) (((i>>>14)&0x7F) | 0x80));
            out.write((int) (((i>>>21)&0x7F) | 0x80));
            out.write((int) (((i>>>28)&0x7F) | 0x80));
            out.write((int) (((i>>>35)&0x7F) | 0x80));
            out.write((int)(i>>>42));
            return 7;
        } else if (i < 1L << 56) {
            out.write((int) ((i&0x7F) | 0x80));
            out.write((int) (((i>>>7)&0x7F) | 0x80));
            out.write((int) (((i>>>14)&0x7F) | 0x80));
            out.write((int) (((i>>>21)&0x7F) | 0x80));
            out.write((int) (((i>>>28)&0x7F) | 0x80));
            out.write((int) (((i>>>35)&0x7F) | 0x80));
            out.write((int) (((i>>>42)&0x7F) | 0x80));
            out.write((int)(i>>>49));
            return 8;
        } else {
            out.write((int) ((i&0x7F) | 0x80));
            out.write((int) (((i>>>7)&0x7F) | 0x80));
            out.write((int) (((i>>>14)&0x7F) | 0x80));
            out.write((int) (((i>>>21)&0x7F) | 0x80));
            out.write((int) (((i>>>28)&0x7F) | 0x80));
            out.write((int) (((i>>>35)&0x7F) | 0x80));
            out.write((int) (((i>>>42)&0x7F) | 0x80));
            out.write((int) (((i>>>49)&0x7F) | 0x80));
            out.write((int)(i>>>56));
            return 9;
        }
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
