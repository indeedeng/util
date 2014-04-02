package com.indeed.util.mmap;

/**
 * @author jplaisance
 */
public abstract class AbstractMemory implements Memory {

    @Override
    public final ByteArray byteArray(long start, long numBytes) {
        return new ByteArray(this, start, numBytes);
    }

    @Override
    public final ShortArray shortArray(long start, long numShorts) {
        return new ShortArray(this, start, numShorts);
    }

    @Override
    public final IntArray intArray(long start, long numInts) {
        return new IntArray(this, start, numInts);
    }

    @Override
    public final LongArray longArray(long start, long numLongs) {
        return new LongArray(this, start, numLongs);
    }

    @Override
    public final FloatArray floatArray(long start, long numFloats) {
        return new FloatArray(this, start, numFloats);
    }

    @Override
    public final DoubleArray doubleArray(long start, long numDoubles) {
        return new DoubleArray(this, start, numDoubles);
    }

    @Override
    public final CharArray charArray(long start, long numChars) {
        return new CharArray(this, start, numChars);
    }
}
