package com.indeed.util.core.sort;

import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * @author jsgroth
 */
public class TestRadixSort {
    @Test
    public void test() {
        Random r = new Random();
        final int n = 10000000;
        int[] test1 = new int[n];
        int[] test2 = new int[n];
        int[] scratch = new int[n];
        int[] countScratch = new int[65536];
        for (int z = 0; z < 10; ++z) {
            for (int i = 0; i < n; i++) {
                int x = r.nextInt();
                test1[i] = x;
                test2[i] = x;
            }
            long elapsed = -System.currentTimeMillis();
            Arrays.sort(test1);
            elapsed += System.currentTimeMillis();
            System.out.println("quicksort elapsed = "+elapsed+"ms");
            elapsed = -System.currentTimeMillis();
            RadixSort.radixSort(test2, n, scratch, countScratch);
            elapsed += System.currentTimeMillis();
            System.out.println("radixsort elapsed = "+elapsed+"ms");
            for (int i = 0; i < n; i ++) {
                assertEquals(test1[i], test2[i]);
            }
        }
    }
}
