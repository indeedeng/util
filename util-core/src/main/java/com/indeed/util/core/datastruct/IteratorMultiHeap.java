package com.indeed.util.core.datastruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;

/** @author jplaisance */
public abstract class IteratorMultiHeap<T> {

    private static final Logger log = LoggerFactory.getLogger(IteratorMultiHeap.class);

    private final T[] elements;

    private int[] candidates;

    private int[] nextCandidates;

    private int size;

    private final int[] minIndexes;

    private final T[] min;

    private int minLength;

    protected IteratorMultiHeap(int capacity, Class<T> tClass) {
        this.elements = (T[]) Array.newInstance(tClass, capacity);
        size = 0;
        candidates = new int[capacity / 2 + (capacity % 2)];
        nextCandidates = new int[candidates.length];
        minIndexes = new int[capacity];
        min = (T[]) Array.newInstance(tClass, capacity);
        minLength = 0;
    }

    protected abstract boolean next(T t);

    protected abstract int compare(T a, T b);

    public final void clear() {
        minLength = 0;
        size = 0;
    }

    public final void add(T t) {
        if (size == elements.length) {
            throw new IllegalStateException("heap is full");
        }
        if (minLength != size) {
            throw new IllegalStateException("must call clear before adding elements");
        }
        elements[size] = t;
        minIndexes[size] = size;
        size++;
        minLength++;
    }

    public final T[] getMin() {
        return min;
    }

    public final int getMinLength() {
        return minLength;
    }

    public final boolean next() {
        if (size == 0) return false;
        for (int i = minLength - 1; i >= 0; i--) {
            final T element = elements[minIndexes[i]];
            if (!next(element)) {
                size--;
                if (size == 0) return false;
                final T tmp = elements[minIndexes[i]];
                elements[minIndexes[i]] = elements[size];
                elements[size] = tmp;
            }
            downHeap(minIndexes[i]);
        }

        int numCandidates = 0;
        if (size > 1) {
            candidates[0] = 1;
            numCandidates = 1;
        }
        if (size > 2) {
            candidates[1] = 2;
            numCandidates = 2;
        }
        minIndexes[0] = 0;
        min[0] = elements[0];
        minLength = 1;
        while (numCandidates > 0) {
            int nextNumCandidates = 0;
            for (int i = 0; i < numCandidates; i++) {
                final int index = candidates[i];
                if (compare(elements[index], elements[0]) == 0) {
                    minIndexes[minLength] = index;
                    min[minLength] = elements[index];
                    minLength++;
                    final int left = index * 2 + 1;
                    if (left < size) {
                        nextCandidates[nextNumCandidates++] = left;
                    }
                    final int right = left + 1;
                    if (right < size) {
                        nextCandidates[nextNumCandidates++] = right;
                    }
                }
            }
            numCandidates = nextNumCandidates;
            final int[] tmp = candidates;
            candidates = nextCandidates;
            nextCandidates = tmp;
        }
        return true;
    }

    private void downHeap(int index) {
        while (true) {
            final int leftIndex = index * 2 + 1;
            final int rightIndex = leftIndex + 1;
            if (leftIndex < size) {
                final int lowerIndex =
                        rightIndex >= size
                                ? leftIndex
                                : (compare(elements[leftIndex], elements[rightIndex]) <= 0
                                        ? leftIndex
                                        : rightIndex);
                if (compare(elements[lowerIndex], elements[index]) < 0) {
                    final T tmp = elements[index];
                    elements[index] = elements[lowerIndex];
                    elements[lowerIndex] = tmp;
                    index = lowerIndex;
                } else {
                    break;
                }
            } else {
                break;
            }
        }
    }
}
