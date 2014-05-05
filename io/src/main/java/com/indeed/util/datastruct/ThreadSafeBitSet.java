package com.indeed.util.datastruct;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Arrays;

/**
 * a bit set that is thread safe readable
 * but not thread safe writable
 * @author ahudson
 */
public class ThreadSafeBitSet implements Serializable {
    private static final long serialVersionUID = -7685178028568216346L;
    private final int [] bits;
    private final int size;

    public ThreadSafeBitSet(int size) {
        this.size = size;
        bits = new int[(size+31)>>>5];
    }

    public final void set(int index, boolean value) {
        if (value) set(index);
        else clear(index);
    }

    public final boolean get(int index) {
        final int t = 1<<(index&0x1F);
        return (bits[index>>5]&t) != 0;
    }

    public final int size() {
        return size;
    }

    public final void set(int index) {
        bits[index>>>5] |= (1<<(index&0x1F));
    }

    public final void clear(int index) {
        bits[index>>>5] &= ~(1<<(index&0x1F));
    }

    public final void clearAll() {
        Arrays.fill(bits, 0);
    }

    public final void setAll() {
        Arrays.fill(bits, 0xFFFFFFFF);
    }

    public final void invertAll() {
        for (int i = 0; i < bits.length; i++) {
            bits[i] = ~bits[i];
        }
    }

    // basically same as java's BitSet.and
    public final void and(ThreadSafeBitSet other) {
        if (other.size != size) throw new IllegalArgumentException("BitSets must be of equal size");
        for (int i = 0; i < bits.length; i++) {
            bits[i] &= other.bits[i];
        }
    }

    /**
     * this = this | other, bitwise
     */
    public final void or(ThreadSafeBitSet other) {
        if (other.size != size) throw new IllegalArgumentException("BitSets must be of equal size");
        for (int i = 0; i < bits.length; i++) {
            bits[i] |= other.bits[i];
        }
    }

    /**
     * this = this ^ other, bitwise
     */
    public final void xor(ThreadSafeBitSet other) {
        if (other.size != size) throw new IllegalArgumentException("BitSets must be of equal size");
        for (int i = 0; i < bits.length; i++) {
            bits[i] ^= other.bits[i];
        }
    }

    public final void copyFrom(ThreadSafeBitSet other) {
        System.arraycopy(other.bits, 0, bits, 0, other.bits.length);
    }

    public final int cardinality() {
        int sum = 0;
        for (int x : bits)
            sum += Integer.bitCount(x);
        return sum;
    }

    public static ThreadSafeBitSet expand(@Nullable ThreadSafeBitSet oldBitSet, int newSize) {
        if (oldBitSet != null && newSize <= oldBitSet.size) return oldBitSet;
        final ThreadSafeBitSet ret = new ThreadSafeBitSet(newSize);
        if (oldBitSet != null) ret.copyFrom(oldBitSet);
        return ret;
    }

    public static boolean equals(ThreadSafeBitSet bitset1, ThreadSafeBitSet bitset2) {
        if (bitset1 == bitset2) return true;
        if (bitset1 == null || bitset2 == null) return false;
        return Arrays.equals(bitset1.bits, bitset2.bits);
    }

    public static ThreadSafeBitSet or(ThreadSafeBitSet a, ThreadSafeBitSet b) {
        final int size = Math.max(a.size(), b.size());
        final ThreadSafeBitSet ret = new ThreadSafeBitSet(size);
        for (int i = 0; i < a.bits.length; i++) {
            ret.bits[i] |= a.bits[i];
        }
        for (int i = 0; i < b.bits.length; i++) {
            ret.bits[i] |= b.bits[i];
        }
        return ret;
    }
}
