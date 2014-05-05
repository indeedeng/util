package com.indeed.util.core;

import com.google.common.primitives.Booleans;
import com.google.common.primitives.Ints;

import javax.annotation.Nonnull;
import java.util.Random;

/**
 * @author ahudson
 */
public class Quicksortables {
    // the sorting code contained in this class was copied from Arrays.java and then modified

    public static Quicksortable getQuicksortableIntArray(final int [] array) {
        return new Quicksortable() {
            public void swap(int i, int j) {
                int t = array[i];
                array[i] = array[j];
                array[j] = t;
            }
            public int compare(int a, int b) {
                int x = array[a];
                int y = array[b];
                if (x < y) return -1;
                if (x == y) return 0;
                return 1;
            }
        };
    }

    public static Quicksortable getQuicksortableParallelIntArrays(final int[] array1, final int[] array2) {
        return new Quicksortable() {
            public void swap(int i, int j) {
                int t = array1[i];
                array1[i] = array1[j];
                array1[j] = t;
                t = array2[i];
                array2[i] = array2[j];
                array2[j] = t;
            }

            public int compare(int a, int b) {
                if (array1[a] < array1[b]) return -1;
                if (array1[a] > array1[b]) return 1;
                if (array2[a] < array2[b]) return -1;
                if (array2[a] > array2[b]) return 1;
                return 0;
            }
        };
    }

    public static Quicksortable getQuicksortableParallelArrays(@Nonnull final long[] array1,
                                                               @Nonnull final int[] array2) {
        return new Quicksortable() {
            public void swap(int i, int j) {
                Quicksortables.swap(array1, i, j);
                Quicksortables.swap(array2, i, j);
            }

            public int compare(int a, int b) {
                if (array1[a] < array1[b]) return -1;
                if (array1[a] > array1[b]) return 1;
                if (array2[a] < array2[b]) return -1;
                if (array2[a] > array2[b]) return 1;
                return 0;
            }
        };
    }

    public static <T extends Comparable<? super T>> Quicksortable getQuicksortableObjectArray(final T [] array) {
        return new Quicksortable() {
            public void swap(int i, int j) {
                T t = array[i];
                array[i] = array[j];
                array[j] = t;
            }
            public int compare(int a, int b) {
                T x = array[a];
                T y = array[b];
                return x.compareTo(y);
            }
        };
    }

    public static Quicksortable getQuicksortableShortArray(final short [] array) {
        return new Quicksortable() {
            public void swap(int i, int j) {
                short t = array[i];
                array[i] = array[j];
                array[j] = t;
            }
            public int compare(int a, int b) {
                short x = array[a];
                short y = array[b];
                if (x < y) return -1;
                if (x == y) return 0;
                return 1;
            }
        };
    }

    public static Quicksortable reverseQuicksortable(final Quicksortable q) {
        return new Quicksortable() {
            public void swap(int i, int j) {
                q.swap(i, j);
            }
            public int compare(int a, int b) {
                return q.compare(b, a);
            }
        };
    }

    public static <T extends Comparable<T>> Quicksortable getQuicksortableParallelComparableIntArrays(final T[] array1, final int[] array2) {
        return new Quicksortable() {
            public void swap(int i, int j) {
                final T t = array1[i];
                array1[i] = array1[j];
                array1[j] = t;
                final int ti = array2[i];
                array2[i] = array2[j];
                array2[j] = ti;
            }

            public int compare(int a, int b) {
                final int cmp = array1[a].compareTo(array1[b]);
                if (cmp == 0) {
                    if (array2[a] < array2[b]) return -1;
                    if (array2[a] > array2[b]) return 1;
                }
                return cmp;
            }
        };
    }

    public static void sort(Quicksortable q, int length) {
        sort1(q, 0, length, length);
    }

    public static void partialSort(Quicksortable q, int k, int length) {
        sort1(q, 0, k, length);
    }

    /**
     * Sorts the specified sub-array of integers into ascending order.
     */
    private static void sort1(Quicksortable q, int off, int k, int len) {
        // we don't care about anything >= to k
        if (off >= k)
            return;
        // Insertion sort on smallest arrays
        if (len < 7) {
            for (int i = off; i < len + off; i++)
                for (int j = i; j > off && q.compare(j, j - 1) < 0; j--)
                    q.swap(j, j - 1);
            return;
        }

        // Choose a partition element, v
        int m = off + (len >> 1);       // Small arrays, middle element
        if (len > 7) {
            int l = off;
            int n = off + len - 1;
            if (len > 40) {        // Big arrays, pseudomedian of 9
                int s = len / 8;
                l = med3(q, l, l + s, l + 2 * s);
                m = med3(q, m - s, m, m + s);
                n = med3(q, n - 2 * s, n - s, n);
            }
            m = med3(q, l, m, n); // Mid-size, med of 3
        }
        // move the pivot element to to the front
        q.swap(off, m);
        m = off;

        // Establish Invariant: m* (<m)* (>m)* m*
        int a = off+1, b = a, c = off + len - 1, d = c;
        int cmp;
        while (true) {
            while (b <= c && (cmp = q.compare(b, off)) <= 0) {
                if (cmp == 0)
                    q.swap(a++, b);
                b++;
            }
            while (c >= b && (cmp = q.compare(c, off)) >= 0) {
                if (cmp == 0)
                    q.swap(c, d--);
                c--;
            }
            if (b > c)
                break;
            q.swap(b++, c--);
        }

        // Swap partition elements back to middle
        int s, n = off + len;
        s = Math.min(a - off, b - a);
        vecswap(q, off, b - s, s);
        s = Math.min(d - c, n - d - 1);
        vecswap(q, b, n - s, s);

        // Recursively sort non-partition-elements
        if ((s = b - a) > 1)
            sort1(q, off, k, s);
        if ((s = d - c) > 1)
            sort1(q, n - s, k, s);
    }

    /**
     * Swaps x[a .. (a+n-1)] with x[b .. (b+n-1)].
     */
    private static void vecswap(Quicksortable q, int a, int b, int n) {
        for (int i = 0; i < n; i++, a++, b++)
            q.swap(a, b);
    }

    /**
     * Returns the index of the median of the three indexed integers.
     */
    private static int med3(Quicksortable q, int a, int b, int c) {
        return (q.compare(a, b) < 0 ?
                (q.compare(b, c) < 0 ? b : q.compare(a, c) < 0 ? c : a) :
                (q.compare(b, c) > 0 ? b : q.compare(a, c) > 0 ? c : a));
    }

    /**
     * sorts the elements in q using the heapsort method
     */
    public static void heapSort(Quicksortable q, int size) {
        q = reverseQuicksortable(q);
        makeHeap(q, size);
        sortheap(q, size);
    }

    /**
     * sorts the heap stored in q
     */
    private static void sortheap(Quicksortable q, int size) {
        for (int i = size-1; i >= 1; i--) {
            q.swap(0, i);
            heapifyDown(q, 0, i);
        }
    }

    /**
     * finds the lowest k elements of q and stores them in sorted order at the beginning of q by using a heap of size k
     */
    public static void partialSortUsingHeap(Quicksortable q, int k, int size) {
        Quicksortable revq = reverseQuicksortable(q);
        makeHeap(revq, k);
        for (int i = k; i < size; i++) {
            if (q.compare(0, i) > 0) {
                q.swap(0, i);
                heapifyDown(revq, 0, k);
            }
        }
        sortheap(revq, k);
    }

    /**
     * finds the lowest k elements of q and stores them in sorted order at the beginning of q by turning q into a heap
     */
    public static void partialHeapSort(Quicksortable q, int k, int size) {
        makeHeap(q, size);
        for (int i = 0; i < k; i++) {
            q.swap(0, size-i-1);
            heapifyDown(q, 0, size-i-1);
        }
        vecswap(q, 0, size-k, k);
        reverse(q, k);
    }


    /**
     * Makes a heap with the elements [0, size) of q
     */
    public static void makeHeap(Quicksortable q, int size) {
        for (int i = (size-1)/2; i >= 0; i--) {
            heapifyDown(q, i, size);
        }
    }


    /**
     * Pushes the last element, located in position 'size-1', into the heap stored in [0, size-2) of q
     */
    public static void pushHeap(Quicksortable q, int size) {
        heapifyUp(q, size-1);
    }

    /**
     * Pops the lowest element off the heap and stores it in the last element
     */
    public static void popHeap(Quicksortable q, int size) {
        q.swap(0, size-1);
        heapifyDown(q, 0, size-1);
    }

    private static void heapifyUp(Quicksortable q, int pos) {
        do {
            if (pos == 0) return;
            int parent = (pos-1)>>1;
            // if parent is less than or equal to current we are done
            if (q.compare(parent, pos) <= 0) return;
            q.swap(parent, pos);
            pos = parent;
        } while (true);
    }

    public static void heapifyDown(Quicksortable q, int pos, int size) {
        do {
            int c = (pos<<1)+1;
            if (c >= size) return;
            // if their is a right child and the right is less than the left then use it
            if (c+1 < size && q.compare(c+1, c) < 0) c++;
            // if the child is greater than or equal to the parent we are done
            if (q.compare(c, pos) >= 0) return;
            q.swap(c, pos);
            pos = c;
        } while (true);
    }

    private final static Random RANDOM = new Random();

    // moves the top k items to the first k positions, in descending sorted order
    // the rest of the entries are preserved in the last totalSize-k entries
    // in an unspecified order
    public static void topK(Quicksortable qs, int totalSize, int k) {
        if (k > totalSize) k = totalSize;
        makeHeap(qs, k);
        for (int i = k; i < totalSize; i++) {
            // compare each element to the root of the heap
            if (qs.compare(i, 0) > 0) {
                // if it's greater, swap it out and push it down
                qs.swap(0, i);
                heapifyDown(qs, 0, k);
            }
        }
        sort(qs, k);
        reverse(qs, k);
    }

    // return value works the same as java's Arrays.binarySearch
    // the compare function on Quicksortable will always pass -1 as the second index, swap function is never called
    public static int binarySearch(Quicksortable qs, int size) {
        int low = 0;
        int high = size-1;

        while (low <= high) {
            int mid = (low + high) >> 1;
            int cmp = qs.compare(mid, -1);

            if (cmp < 0)
            low = mid + 1;
            else if (cmp > 0)
            high = mid - 1;
            else
            return mid; // key found
        }
        return -(low + 1);  // key not found.
    }

    public static void shuffle(Quicksortable q, int size) {
        shuffle(q, 0, size, RANDOM);
    }

    public static void shuffle(Quicksortable q, int off, int size, Random rnd) {
        for (int i=size; i>1; i--)
            q.swap(i-1+off, rnd.nextInt(i)+off);
    }

    public static void reverse(Quicksortable q, int size) {
        for (int i = 0; i < size/2; i++) {
            q.swap(i, size-i-1);
        }
    }

    public static boolean isSorted(Quicksortable q, int size) {
        for (int i = 0; i+1 < size; i++)
            if (q.compare(i, i+1) > 0) return false;
        return true;
    }

    public static int compare(String[] a, int i, int j) {
        return a[i].compareTo(a[j]);
    }

    public static int compare(float[] a, int i, int j) {
        return Float.compare(a[i], a[j]);
    }

    public static int compare(double[] a, int i, int j) {
        return Double.compare(a[i], a[j]);
    }

    public static int compare(int[] a, int i, int j) {
        return Ints.compare(a[i], a[j]);
    }

    public static int compare(boolean[] a, int i, int j) {
        return Booleans.compare(a[i], a[j]);
    }

    public static void swap(Object[] a, int i, int j) {
        Object t = a[i];
        a[i] = a[j];
        a[j] = t;
    }

    public static void swap(double[] a, int i, int j) {
        double t = a[i];
        a[i] = a[j];
        a[j] = t;
    }

    public static void swap(float[] a, int i, int j) {
        float t = a[i];
        a[i] = a[j];
        a[j] = t;
    }

    public static void swap(int[] a, int i, int j) {
        int t = a[i];
        a[i] = a[j];
        a[j] = t;
    }

    public static void swap(long[] a, int i, int j) {
        long t = a[i];
        a[i] = a[j];
        a[j] = t;
    }

    public static void swap(byte[] a, int i, int j) {
        byte t = a[i];
        a[i] = a[j];
        a[j] = t;
    }

    public static void swap(boolean[] a, int i, int j) {
        boolean t = a[i];
        a[i] = a[j];
        a[j] = t;
    }

    public static String[] copy(String[] a) {
        return copy(a, 0, a.length);
    }

    public static String[] copy(String[] a, int i, int j) {
        String[] r = new String[j-i];
        System.arraycopy(a, i, r, 0, r.length);
        return r;
    }

    public static double[] copy(double[] a) {
        return copy(a, 0, a.length);
    }

    public static double[] copy(double[] a, int i, int j) {
        double[] r = new double[j-i];
        System.arraycopy(a, i, r, 0, r.length);
        return r;
    }

    public static float[] copy(float[] a) {
        return copy(a, 0, a.length);
    }

    public static float[] copy(float[] a, int i, int j) {
        float[] r = new float[j-i];
        System.arraycopy(a, i, r, 0, r.length);
        return r;
    }

    public static int[] copy(int[] a) {
        return copy(a, 0, a.length);
    }

    public static int[] copy(int[] a, int i, int j) {
        int[] r = new int[j-i];
        System.arraycopy(a, i, r, 0, r.length);
        return r;
    }
}
