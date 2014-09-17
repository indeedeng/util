package com.indeed.util.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Random;

/**
 * @author jack@indeed.com (Jack Humphrey)
 */
public class ReleaseVersionBenchmark {

    private static void fillRandom(int[] numbers, int max, Random random) {
        for (int i = 0; i < numbers.length; i++) {
            numbers[i] = random.nextInt(max);
        }
    }

    public static void main(String[] args) throws Exception {
        final int size = Integer.valueOf(args[0]);
        final ReleaseVersion.MatchPrecision lhsMatchPrecision =
                ReleaseVersion.MatchPrecision.forLength(Integer.valueOf(args[1]));
        final File resultOutputFile = args.length > 2 ? new File(args[2]) : null;

        System.out.println(size + " comparisons with left-hand side precision " + lhsMatchPrecision);

        final Random random = new Random(52473); // deterministic random output

        final int[] majors = new int[size];
        final int[] minors = new int[size];
        final int[] patches = new int[size];
        final int[] builds = new int[size];
        fillRandom(majors, 0x7FFF, random);
        fillRandom(minors, 0x7FFF, random);
        fillRandom(patches, 0x7FFF, random);
        fillRandom(builds, 0x7FFF, random);
        final String[] strs = new String[size];
        for (int i = 0; i < strs.length; i++) {
            strs[i] = 
                    random.nextInt(0x7FFF) + "." +
                    random.nextInt(0x7FFF) + "." +
                    random.nextInt(0x7FFF) + "." +
                    random.nextInt(0x7FFF);
        }
        System.out.println("test version data created");

        long createLhs = -System.nanoTime();
        ReleaseVersion[] lhs = new ReleaseVersion[size];
        for (int i = 0; i < lhs.length; i++) {
            lhs[i] = ReleaseVersion.newBuilder()
                    .setMajorVersion(majors[i])
                    .setMinorVersion(minors[i])
                    .setPatchVersion(patches[i])
                    .setBuildNumber(builds[i])
                    .setMatchPrecision(lhsMatchPrecision)
                    .build();
        }
        createLhs += System.nanoTime();
        System.out.println("create lhs versions (ns): " + createLhs);

        long createRhs = -System.nanoTime();
        ReleaseVersion[] rhs = new ReleaseVersion[size];
        for (int i = 0; i < rhs.length; i++) {
            rhs[i] = ReleaseVersion.fromString(strs[i]);
        }
        createRhs += System.nanoTime();
        System.out.println("parse rhs versions (ns): " + createRhs);

        int[] results = new int[size];
        long compareV1 = -System.nanoTime();
        for (int i = 0; i < size; i++) {
            if (size < 10) System.out.println(lhs[i] + "," + rhs[i]);
            results[i] = lhs[i].compareTo(rhs[i]);
        }
        compareV1 += System.nanoTime();
        final double average = ((double) compareV1) / results.length;
        System.out.println("comparison runtime (ns): " + compareV1);
        System.out.println("number of comparisons: " + results.length);
        System.out.println("average comparison time (ns): " + average);

        if (resultOutputFile != null) {
            final ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(resultOutputFile));
            out.writeObject(results);
            out.close();
        }
    }

}
