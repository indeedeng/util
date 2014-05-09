package com.indeed.util.core.sort;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Random;

/**
 * @author ahudson
 */
public class TestQuicksortables extends TestCase {
    public void testTopK() {
        int n = 1000;
        int k = 10;

        int[] values = new int[n];
        for (int i = 0; i < n; i++) values[i] = i;

        Quicksortable qs = Quicksortables.getQuicksortableIntArray(values);
        Quicksortables.shuffle(qs, n);
        Quicksortables.topK(qs, n, k);
        for (int i = 0; i < k; i++) {
            assertEquals(n-i-1, values[i]);
        }

        Quicksortables.shuffle(qs, n);
        Quicksortables.topK(qs, n, n*2);
        for (int i = 0; i < n; i++) {
            assertEquals(n-i-1, values[i]);
        }

        // tests for exceptions
        Quicksortables.shuffle(qs, n);
        Quicksortables.topK(qs, n, 0);
    }

    private static void testVersesJavaBinarySearch(final int target, final int[] values) {
        int pos1 = Arrays.binarySearch(values, target);
        int pos2 = Quicksortables.binarySearch(new Quicksortable() {
            public void swap(int i, int j) {
                assertTrue("This should never be called", false);
            }
            public int compare(int i, int j) {
                assertEquals(-1, j);
                if (values[i] < target) return -1;
                if (values[i] > target) return 1;
                else return 0;
            }
        }, values.length);
        assertEquals(pos1, pos2);
    }

    public void testBinarySearch() {
        int n = 100000;
        int[] values = new int[n];
        Random r = new Random();
        for (int i = 0; i < n; i++) values[i] = r.nextInt();
        Arrays.sort(values);

        for (int value : values) testVersesJavaBinarySearch(value, values);
        for (int i = 0; i < n; i++) testVersesJavaBinarySearch(r.nextInt(), values);

        for (int i = 0; i < n; i++) values[i] = 0;
        for (int value : values) testVersesJavaBinarySearch(value, values);
    }

    public void testQuicksortableObjectArray() {
        final int n = 5000;
        Double[] foo = new Double[n];
        Quicksortable qs = Quicksortables.getQuicksortableObjectArray(foo);
        for (int i = 0; i < n; i++) {
            foo[i] = (double)i;
        }
        for (int k = 0; k < 3; k++) {
            for (int i = 0; i < 5; i++) {
                long elapsed = 0;
                for (int j = 0; j < 100; j++) {
                    Quicksortables.shuffle(qs, n);
                    elapsed -= System.currentTimeMillis();
                    Arrays.sort(foo);
                    elapsed += System.currentTimeMillis();
                    assertSorted(foo);
                }
                //System.out.println("java sort: elapsed = "+elapsed+"ms");
            }
            for (int i = 0; i < 5; i++) {
                long elapsed = 0;
                for (int j = 0; j < 100; j++) {
                    Quicksortables.shuffle(qs, n);
                    elapsed -= System.currentTimeMillis();
                    Quicksortables.sort(qs, n);
                    elapsed += System.currentTimeMillis();
                    assertSorted(foo);
                }
                //System.out.println("quicksortables sort: elapsed = "+elapsed+"ms");
            }
        }
    }

    private static void assertSorted(Double[] list) {
        for (int i = 0; i < list.length; i++) {
            assertEquals((double)i, list[i]);
        }
    }
}
