package com.indeed.util.core.threads;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Arrays;

/**
 * a bit set that is thread safe readable but not thread safe writable
 *
 * @author ahudson
 */
public class ThreadSafeBitSet implements Serializable {
    private static final long serialVersionUID = -7685178028568216346L;
    private final int[] bits;
    private final int size;

    public ThreadSafeBitSet(int size) {
        this.size = size;
        this.bits = new int[(size + 31) >>> 5];
    }

    private ThreadSafeBitSet(int[] bits, int size) {
        this.bits = bits;
        this.size = size;
    }

    /* mask for the last byte that contains 1 on the bits that
     * are actually in use.
     */
    private int finalIntUsedBitMask() {
        final int nbUsedBitsInLastInt = this.size % 32;
        final int nbUselessBits = (32 - nbUsedBitsInLastInt) % 32;
        return (~0 >>> nbUselessBits);
    }

    private void cleanUpLastInt() {
        final int nbUsedBitsInLastInt = this.size % 32;
        if (nbUsedBitsInLastInt > 0) {
            this.bits[this.bits.length - 1] &= finalIntUsedBitMask();
        }
    }

    public final void set(int index, boolean value) {
        if (value) set(index);
        else clear(index);
    }

    public final boolean get(int index) {
        final int t = 1 << (index & 0x1F);
        return (bits[index >> 5] & t) != 0;
    }

    public ThreadSafeBitSet copy() {
        return new ThreadSafeBitSet(this.bits.clone(), this.size);
    }

    public final int size() {
        return size;
    }

    public final void set(int index) {
        bits[index >>> 5] |= (1 << (index & 0x1F));
    }

    public final void clear(int index) {
        bits[index >>> 5] &= ~(1 << (index & 0x1F));
    }

    public final void clearAll() {
        Arrays.fill(bits, 0);
    }

    public final void setAll() {
        Arrays.fill(bits, 0xFFFFFFFF);
        cleanUpLastInt();
    }

    public final void invertAll() {
        for (int i = 0; i < bits.length; i++) {
            bits[i] = ~bits[i];
        }
        cleanUpLastInt();
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
     *
     * @param other The bit set to or with.
     */
    public final void or(ThreadSafeBitSet other) {
        if (other.size != size) throw new IllegalArgumentException("BitSets must be of equal size");
        for (int i = 0; i < bits.length; i++) {
            bits[i] |= other.bits[i];
        }
    }

    /**
     * this = this ^ other, bitwise
     *
     * @param other The bit set to xor with.
     */
    public final void xor(ThreadSafeBitSet other) {
        if (other.size != size) throw new IllegalArgumentException("BitSets must be of equal size");
        for (int i = 0; i < bits.length; i++) {
            bits[i] ^= other.bits[i];
        }
    }

    public final void copyFrom(ThreadSafeBitSet other) {
        if (other.size == this.size) {
            System.arraycopy(other.bits, 0, bits, 0, other.bits.length);
        } else if (other.size < this.size) {
            if (other.size > 0) {
                // we copy the array but handle the last int separately
                System.arraycopy(other.bits, 0, bits, 0, other.bits.length - 1);
                final int otherLastInt = other.bits[other.bits.length - 1];
                final int myLastModifiedInt = this.bits[other.bits.length - 1];
                final int mask = other.finalIntUsedBitMask();
                final int lastInt = (mask & otherLastInt) | ((~mask) & myLastModifiedInt);
                this.bits[other.bits.length - 1] = lastInt;
            }
        } else {
            throw new IllegalArgumentException(
                    "Copy from array bigger than destination is forbidden");
        }
    }

    public final void copyFromRange(
            final ThreadSafeBitSet other,
            final int startIndex,
            final int otherStartIndex,
            final int length) {

        if (length == 0) {
            return;
        }

        // end indices are INCLUSIVE to make the twiddling a tad simpler
        final int endIndex = startIndex + length - 1;
        final int otherEndIndex = otherStartIndex + length - 1;

        final int bitsStartIndex = startIndex >>> 5;
        final int otherBitsStartIndex = otherStartIndex >>> 5;

        final int bitsEndIndex = endIndex >>> 5;

        if ((startIndex & 0x1F) == (otherStartIndex & 0x1F)) {
            if (bitsStartIndex != bitsEndIndex) {
                System.arraycopy(
                        other.bits,
                        otherBitsStartIndex + 1,
                        bits,
                        bitsStartIndex + 1,
                        bitsEndIndex - bitsStartIndex - 1);

                simpleCopyFromRange(
                        other, startIndex, otherStartIndex, Integer.SIZE - (startIndex & 0x1F));
                simpleCopyFromRange(
                        other, endIndex & ~0x1F, otherEndIndex & ~0x1F, (endIndex & 0x1F) + 1);
            } else {
                simpleCopyFromRange(other, startIndex, otherStartIndex, length);
            }
        } else {
            if (bitsStartIndex != bitsEndIndex) {
                final int difference = (startIndex & 0x1F) - (otherStartIndex & 0x1F);
                final int absDifference = Math.abs(difference);
                final int reverseDifference = Integer.SIZE - absDifference;
                final int reverseDifferenceMask = -1 << reverseDifference;
                final int absDifferenceMask = -1 << absDifference;

                if (difference > 0) {
                    bits[bitsStartIndex] =
                            (bits[bitsStartIndex] & ~(-1 << (startIndex & 0x1F)))
                                    | ((other.bits[otherBitsStartIndex]
                                                    & (-1 << (otherStartIndex & 0x1F)))
                                            << difference);
                    for (int bitsIndex = bitsStartIndex + 1, otherBitsIndex = otherBitsStartIndex;
                            bitsIndex < bitsEndIndex;
                            ++bitsIndex, ++otherBitsIndex) {
                        bits[bitsIndex] =
                                ((other.bits[otherBitsIndex] & reverseDifferenceMask)
                                                >>> reverseDifference)
                                        | ((other.bits[otherBitsIndex + 1] & ~reverseDifferenceMask)
                                                << difference);
                    }
                    simpleCopyFromRange(
                            other,
                            endIndex & ~0x1F,
                            otherEndIndex - (endIndex & 0x1F),
                            (endIndex & 0x1F) + 1);
                } else {
                    simpleCopyFromRange(
                            other, startIndex, otherStartIndex, Integer.SIZE - (startIndex & 0x1F));
                    for (int bitsIndex = bitsStartIndex + 1,
                                    otherBitsIndex = otherBitsStartIndex + 1;
                            bitsIndex < bitsEndIndex;
                            ++bitsIndex, ++otherBitsIndex) {
                        bits[bitsIndex] =
                                ((other.bits[otherBitsIndex] & absDifferenceMask) >>> absDifference)
                                        | ((other.bits[otherBitsIndex + 1] & ~absDifferenceMask)
                                                << reverseDifference);
                    }
                    simpleCopyFromRange(
                            other,
                            endIndex & ~0x1F,
                            otherEndIndex - (endIndex & 0x1F),
                            (endIndex & 0x1F) + 1);
                }
            } else {
                simpleCopyFromRange(other, startIndex, otherStartIndex, length);
            }
        }
    }

    private void simpleCopyFromRange(
            final ThreadSafeBitSet other,
            final int startIndex,
            final int otherStartIndex,
            final int length) {
        for (int i = 0; i < length; ++i) {
            set(startIndex + i, other.get(otherStartIndex + i));
        }
    }

    public final int cardinality() {
        int sum = 0;
        for (final int x : bits) {
            sum += Integer.bitCount(x);
        }
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
        if (bitset1.size() != bitset2.size()) return false;
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
