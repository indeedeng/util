package com.indeed.util.core.sort;

import java.util.Arrays;

public class RadixSort {
    public static void radixSort(int[] input, int n, int[] scratch, int[] countScratch) {
        if (countScratch.length != 65536)
            throw new IllegalArgumentException("countScratch.length must be = 65536");
        if (input.length < n) throw new IllegalArgumentException("input.length must be >= n");
        if (scratch.length < n) throw new IllegalArgumentException("scratch.length must be >= n");

        Arrays.fill(countScratch, 0);
        for (int i = 0; i < n; i++) countScratch[input[i] & 0xFFFF]++;
        int sum = 0;
        for (int i = 0; i < countScratch.length; i++) {
            int temp = sum;
            sum += countScratch[i];
            countScratch[i] = temp;
        }
        for (int i = 0; i < n; i++) {
            int temp = input[i] & 0xFFFF;
            int offset = countScratch[temp]++;
            scratch[offset] = input[i];
        }

        Arrays.fill(countScratch, 0);
        for (int i = 0; i < n; i++) countScratch[(scratch[i] >> 16) + 32768]++;
        sum = 0;
        for (int i = 0; i < countScratch.length; i++) {
            int temp = sum;
            sum += countScratch[i];
            countScratch[i] = temp;
        }
        for (int i = 0; i < n; i++) {
            int temp = (scratch[i] >> 16) + 32768;
            int offset = countScratch[temp]++;
            input[offset] = scratch[i];
        }
    }
}
