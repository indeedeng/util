package com.indeed.util.urlparsing.benchmark;

import com.google.common.base.Stopwatch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * @author: preetha
 */
public class KeyValueParsingBenchmark {


    public static void main(String[] args) throws IOException {
        final KeyValueParser kpv;
        if (args.length> 0 &&  "ind".equals(args[0]) ) {
            kpv = new IndeedKeyValueParser();
        } else {
            kpv = new StringSplitKeyValueParser();
        }

        runBenchMark(kpv);

    }

    private static void runBenchMark(final KeyValueParser kvParser) throws IOException {

        final BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(KeyValueParsingBenchmark.class.getResourceAsStream("/logentries.txt.gz"))));

        final Stopwatch stopwatch = Stopwatch.createUnstarted();

        String log = "";
        int count = 0;
        while ((log = reader.readLine()) != null) {
            stopwatch.start();
            kvParser.parse(log);
            stopwatch.stop();
            count++;
        }
        reader.close();

        System.out.println("Parsed "+ count+" url params in "+  stopwatch);
        final List<GarbageCollectorMXBean> garbageCollectorMXBeans = ManagementFactory.getGarbageCollectorMXBeans();

        for (GarbageCollectorMXBean mxBean : garbageCollectorMXBeans) {
            System.out.println(mxBean.getName()+"\t"+mxBean.getCollectionCount()+" gc collections in \t"+mxBean.getCollectionTime()+ " milliseconds");
        }
    }


}
